module m1;
public class A{
	m2a::B b1 = new m2a::B();
	m2b::B b2 = new m2b::B();

	m3a::C c1 = new m3a::C();
	m3b::C c2 = new m3b::C();

	public A() {
		System.out.println(this.getClass());
	}
}
