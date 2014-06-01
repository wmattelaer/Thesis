

object(e, undefined, edge, X, 0, -1) :- member(X, [0, 1, 2, 3, 4, 5, 6, 7]).
object(e, undefined, edge, X, 7, -1) :- member(X, [0, 1, 2, 3, 4, 5, 6, 7]).
object(e, undefined, edge, 0, Y, -1) :- member(Y, [0, 1, 2, 3, 4, 5, 6, 7]).
object(e, undefined, edge, 7, Y, -1) :- member(Y, [0, 1, 2, 3, 4, 5, 6, 7]).

object(c1, undefined, corner, 0, 0, 0).
object(c2, undefined, corner, 7, 0, 0).
object(c3, undefined, corner, 0, 7, 0).
object(c4, undefined, corner, 7, 7, 0).

object(b, undefined, board, X, Y, -1) :- member(X, [0, 1, 2, 3, 4, 5, 6, 7]), member(Y, [0, 1, 2, 3, 4, 5, 6, 7]).

%object(O, C, stack, X, Y, 0) :- object(O, C, cube, X, Y, _), object(O2, _, cube, X, Y, _), O \= O2.
%object(O, C, cube-group, X, Y, 0) :- object(O, C, cube, X, Y, _), object(O2, _, cube, X, Y, _), O \= O2.

type(O, X) :- object(O, _, X, _, _, _).
type(O, stack) :- object(O, C, cube, X, Y, _), object(O2, _, cube, X, Y, _), O \= O2.
type(O, cube-group) :- object(O, C, cube, X, Y, _), object(O2, _, cube, X, Y, _), O \= O2.

color(O, X) :- object(O, X, _, _, _, _).

relation(O1, above, O2) :- object(O1, _, _, X, Y, Z1), Z2 is Z1-1, object(O2, _, _, X, Y, Z2).
relation(O1, below, O2) :- object(O1, _, _, X, Y, Z1), Z2 is Z1+1, object(O2, _, _, X, Y, Z2).

relation(O1, left, O2) :- object(O1, _, _, X, Y1, _), Y2 is Y1-1, object(O2, _, _, X, Y2, _).
relation(O1, right, O2) :- object(O1, _, _, X, Y1, _), Y2 is Y1+1, object(O2, _, _, X, Y2, _).

relation(O1, within, O2) :- object(O1, _, _, X, Y, _), object(O2, _, _, X, Y, _).

relation(O1, nearest, O2) :- object(O1, _, T, X1, Y1, _), object(O2, _, _, X2, Y2, _), O1 \= O2, Dist is (X2-X1)*(X2-X1)+(Y2-Y1)*(Y2-Y1), \+((object(O, _, T, X, Y, _), O \= O1, O \= O2, X \= X2, Y \= Y2, D is (X2-X)*(X2-X)+(Y2-Y)*(Y2-Y), D < Dist)).

indicator(O, left) :- object(O, _, T, _, Y1, _), \+((object(_, _, T, _, Y2, _), Y2 > Y1)).
indicator(O, right) :- object(O, _, T, _, Y1, _), \+((object(_, _, T, _, Y2, _), Y2 < Y1)).
indicator(O, leftmost) :- object(O, _, T, _, Y1, _), \+((object(_, _, T, _, Y2, _), Y2 > Y1)).
indicator(O, rightmost) :- object(O, _, T, _, Y1, _), \+((object(_, _, T, _, Y2, _), Y2 < Y1)).
indicator(O, front) :- object(O, _, T, X1, _, _), \+((object(_, _, T, X2, _, _), X2 > X1)).
indicator(O, back) :- object(O, _, T, X1, _, _), \+((object(_, _, T, X2, _, _), X2 < X1)).
indicator(O, individual) :- object(O, _, T, X1, Y1, Z1), \+((object(_, _, T, X, Y1, Z1), (X is X1+1; X is X1-1))), \+((object(_, _, T, X1, Y, Z1), (Y is Y1+1; Y is Y1-1))), \+((object(_, _, T, X1, Y1, Z), (Z is Z1+1; Z is Z1-1))).


region(O, right) :- object(O, _, _, _, 0, _).
region(O, right) :- object(O, _, _, _, 1, _).
region(O, left) :- object(O, _, _, _, 6, _).
region(O, left) :- object(O, _, _, _, 7, _).
region(O, front) :- object(O, _, _, 6, _, _).
region(O, front) :- object(O, _, _, 7, _, _).
region(O, back) :- object(O, _, _, 0, _, _).
region(O, back) :- object(O, _, _, 1, _, _).
region(O, center) :- object(O, _, _, 3, 3, _).
region(O, center) :- object(O, _, _, 3, 4, _).
region(O, center) :- object(O, _, _, 4, 3, _).
region(O, center) :- object(O, _, _, 4, 4, _).


constraint :- object(O1, _, _, X, Y, Z), object(O2, _, _, X, Y, Z), O1 \= O2.
evidence(constraint, false).

