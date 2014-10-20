.class public test
.super java/lang/Object
	
	
	; standard class static initializer 
.method static <clinit>()V
	
	
	; set limits used by this method
.limit locals 0
.limit stack 0
	return
.end method
	
	; standard constructor initializer 
.method public <init>()V
.limit stack 1
.limit locals 1
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest; from L0 to L1
	new test
	dup
	invokenonvirtual test/<init>()V
	astore_1
.line 2
	iconst_2
	newarray int
	astore_2
.line 3
.line 3
	aload_2
	iconst_0
	iconst_2
	iastore
	invokestatic VC/lang/System/putIntLn(I)V
L3:
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 4
.end method
