#!/usr/bin/perl -w

use strict;

use XML::XPath;
use XML::XPath::XMLParser;

my %oldfailed=();
my %oldpassed=();
my %oldskipped=();
my %newfailed=();
my %newpassed=();
my %newskipped=();

sub load {
  my ($file,$result)=@_;
  my $data=XML::XPath->new(filename=>$file);
  my $nodeset=$data->find('/suite/ajc-test');
  foreach my $node ($nodeset->get_nodelist) {
    my $str=XML::XPath::XMLParser::as_string($node);
    my ($num)=($str=~/num=\"(\d+)\"/m);
    $num='nonum' unless defined $num;
    my ($dir,$title)=($str=~/dir=\"(.*?)\".*title=\"(.*?)\"/m);
    ($title,$dir)=($str=~/title=\"(.*?)\".*dir=\"(.*?)\"/m) unless defined $title;
    if(exists $result->{"$num: $dir - $title"}) { print STDERR "duplicated: $num: $dir - $title\n"; }
    $result->{"$num: $dir - $title"}=1;
  }
}

sub dodiffnum {
  my ($new,$old)=@_;
  my $newcount=scalar (keys %$new);
  my $oldcount=scalar (keys %$old);
  my $diff=$newcount-$oldcount;

  print "$newcount (";
  if($diff>0) {
    print "up ".$diff;
  } elsif($diff<0) {
    print "down ".(-$diff);
  } else {
    print "no change";
  }
  print ")\n";
  return $diff;
}

sub dodiff {
  my ($new,$old)=@_;
  foreach my $key (keys %$old) {
    if(exists $new->{$key}) {
      delete $new->{$key};
    } else {
      $new->{$key}=-1;
    }
  }
  print "\nLost:\n";
  foreach my $key (sort (keys %$new)) {
    if($new->{$key}==1) {
      print "$key\n";
    }
  }
  print "\nGained:\n";
  foreach my $key (sort (keys %$new)) {
    if($new->{$key}==-1) {
      print "$key\n";
    }
  }
}

&load('failed_current.xml',\%oldfailed);
&load('failed.xml',\%newfailed);
&load('passed_current.xml',\%oldpassed);
&load('passed.xml',\%newpassed);
&load('skipped_current.xml',\%oldskipped);
&load('skipped.xml',\%newskipped);

# Yes, I know this bit is a horrible hack. Please
# rewrite the whole lot!
if (scalar @ARGV > 0) {
   my ($address) = $ARGV[0];
   my $subject = "test run results";
   if (scalar @ARGV > 1) {
      $subject = $ARGV[1];
   }
   open DEVNULL, "> /dev/null";
   select(DEVNULL);
   my $bracketopen = 0;
   my $failnum = &dodiffnum(\%newfailed, \%oldfailed);
   if ($failnum != 0) {
      if (!$bracketopen) {
         $subject .= " (";
         $bracketopen = 1;
      } else {
         $subject .= ", ";
      }
      my $sign = $failnum < 0 ? '' : '+';
      $subject .= "$sign${failnum}f";
   }
   my $passnum = &dodiffnum(\%newpassed, \%oldpassed);
   if ($passnum != 0) {
      if (!$bracketopen) {
         $subject .= " (";
         $bracketopen = 1;
      } else {
         $subject .= ",";
      }
      my $sign = $passnum < 0 ? '' : '+';
      $subject .= "$sign${passnum}p";
   }
   my $skipnum = &dodiffnum(\%newskipped, \%oldskipped);
   if ($skipnum != 0) {
      if (!$bracketopen) {
         $subject .= " (";
         $bracketopen = 1;
      } else {
         $subject .= ", ";
      }
      my $sign = $skipnum < 0 ? '' : '+';
      $subject .= "$sign${skipnum}s";
   }
   if ($bracketopen) {
      $subject .= ")";
   } else {
      $subject .= " (no changes)";
   }
   close DEVNULL;

#don't send email for now
#
#  open MAIL, qq{| mail -s "$subject" $address};
#
#  select(MAIL);
}

print "Pass: ";
&dodiffnum(\%newpassed,\%oldpassed);

print "Fail: ";
&dodiffnum(\%newfailed,\%oldfailed);

print "Skipped: ";
&dodiffnum(\%newskipped,\%oldskipped);

&dodiff(\%newfailed,\%oldfailed);

#append failed.out
open FAILED, "< failed.out";
print <FAILED>;
