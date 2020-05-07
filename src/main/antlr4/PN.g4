// Define a grammar called model
grammar PN;
@header{
package inequalizer;
import inequalizer.petriNet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
}
@parser::members
{
String test="test";
PetriNet net=new PetriNet();
int n=0;
}
pn returns [PetriNet netz]: {$netz =net;} expected? vars rules m0 target;

expected : '#expected result: safe' {net.expected=true;}| '#expected result: unsafe' {net.expected=true;};
vars : 'vars' (n=NAME {net.addPlace($n.text);n++;})+;
rules : 'rules' (t=trans {net.addTransition($t.value);})+;
m0 : 'init' m=marking {Inequalizer.m0=new Marking($m.value);};
target :
 'target' m1=marking {Inequalizer.mF=new Marking($m1.value);} 
| m2=init {Inequalizer.mF=new Marking($m2.value);};

trans returns [PNTransition value]: t1=init ARROW t2=tdelta SEMI 
{$value = new PNTransition($t1.value,$t2.value);};

tdelta returns [List<Integer> value]: 
{$value =new ArrayList<>(n);for(int i=0;i<n;i++){$value.add(0);}} 
(NAME APO EQ n=NAME nr=number COMMA{$value.set(net.getPlaceNr($n.text),$nr.value);})* 
NAME APO EQ m=NAME nr2=number{$value.set(net.getPlaceNr($m.text),$nr2.value);};

init returns [List<Integer> value]: 
{$value =new ArrayList<>(n);for(int i=0;i<n;i++){$value.add(0);}} (n=NAME GEQ nr=posnumber COMMA{$value.set(net.getPlaceNr($n.text),$nr.value);})* 
m=NAME GEQ nr2=posnumber{$value.set(net.getPlaceNr($m.text),$nr2.value);};

posnumber returns [int value] :  nr=PNUMBER {$value=Integer.parseInt($nr.text);};
number returns [int value] : pm=POSNEG nr=PNUMBER {$value=Integer.parseInt($pm.text+$nr.text);};

marking returns [List<Integer> value]: 
{$value = new ArrayList<>(n);for(int i=0;i<n;i++){$value.add(0);}} 
(n=NAME (EQ | GEQ) nr=posnumber COMMA  {$value.set(net.getPlaceNr($n.text),$nr.value);})* m=NAME (EQ | GEQ) nr2=posnumber {$value.set(net.getPlaceNr($m.text),$nr2.value);};

PNUMBER : [0-9]+ ; 
EQ : '=' ;
APO : '\'' ;
GEQ : '>=' ;
POSNEG :[+\-]+ ; 
//NUMBER :[+\-0-9]+ ; 
ARROW : '->' ;
SEMI : ';' ;
COMMA : ',' ;
NAME : [A-Za-z0-9_]+ ;        // match identifiers
COMMENT : '#' .*? '\n' -> skip ;
INVARIANTS : 'invariants' .*? -> skip ;
WS : [ \t\n\r]+ -> skip ; // skip spaces, tabs, newlines
