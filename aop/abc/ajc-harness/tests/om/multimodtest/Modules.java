
module ModuleA {
    class A;
    open ModuleB;
    open ModuleC;
    advertise() call(* f1(..));
}

module ModuleB {
    class B;
    advertise() call(* f2(..));
}

module ModuleC {
    class C;
    advertise() call(* f3(..));
}