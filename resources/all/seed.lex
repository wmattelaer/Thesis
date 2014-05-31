the :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> $0))
the other :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> $0))
a :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> $0))
an :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> $0))
another :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> $0))


// Colors

red :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 red:co))
green :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 green:co))
blue :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 blue:co))
purple :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 magenta:co))
turquoise :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 cyan:co))
white :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 white:co))
yellow :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 yellow:co))
grey :- ADJ : (lambda $0:e (color:<ent,<co,t>> $0 gray:co))

red-green :- ADJ : (lambda $0:e (and:<t*,t> (color:<ent,<co,t>> $0 red:co) (color:<ent,<co,t>> $0 green:co)))

red :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 red:co) ($0 $1)))))
green :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 green:co) ($0 $1)))))
blue :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) ($0 $1)))))
purple :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 magenta:co) ($0 $1)))))
turquoise :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 cyan:co) ($0 $1)))))
white :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 white:co) ($0 $1)))))
yellow :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 yellow:co) ($0 $1)))))
grey :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 gray:co) ($0 $1)))))

and :- (ADJ\ADJ)/ADJ : (lambda $2:<e,t> (lambda $1:<e,t> (lambda $0:e (and:<t*,t> ($1 $0) ($2 $0)))))


// Types

block :- N : (lambda $0:e (type:<ent,<typ,t>> $0 cube:typ))
cube :- N : (lambda $0:e (type:<ent,<typ,t>> $0 cube:typ))
pyramid :- N : (lambda $0:e (type:<ent,<typ,t>> $0 prism:typ))

blocks :- N : (lambda $0:e (type:<ent,<typ,t>> $0 cube-group:typ))
cubes :- N : (lambda $0:e (type:<ent,<typ,t>> $0 cube-group:typ))
tower :- N : (lambda $0:e (type:<ent,<typ,t>> $0 stack:typ))

edge :- N : (lambda $0:e (type:<ent,<typ,t>> $0 edge:typ))
corner :- N : (lambda $0:e (type:<ent,<typ,t>> $0 corner:typ))
robot :- N : (lambda $0:e (type:<ent,<typ,t>> $0 robot:typ))

square :- N : (lambda $0:e (type:<ent,<typ,t>> $0 tile:typ))

back :- N : (lambda $0:e (and:<t*,t> (indicator:<ent,<ind,t>> $0 back:ind) (type:<ent,<typ,t>> $0 region:typ)))
front :- N : (lambda $0:e (and:<t*,t> (indicator:<ent,<ind,t>> $0 front:ind) (type:<ent,<typ,t>> $0 region:typ)))

// Indicator

nearest :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 nearest:ind))
single :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 individual:ind))

center of the :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 center:ind))
top :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 front:ind))
bottom :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 back:ind))
left :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 left:ind))
right :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 right:ind))

left most :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 leftmost:ind))
right most :- ADJ : (lambda $0:e (indicator:<ent,<ind,t>> $0 rightmost:ind))

// Actions

pick up :- S/NP : (lambda $1:e (lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act $1)))

move :- ((S/NP)/PP)/NP : (lambda $3:e (lambda $2:e (lambda $1:e (lambda $0:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $0 move:act $3) (destination:<evt,<rel,<ent,t>>> $0 $2 $1))))))

move :- S/NP : (lambda $1:e (lambda $0:e (action:<evt,<act,<ent,t>>> $0 move:act $1)))

to the :- (S\S)/NP/PP : (lambda $3:e (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> ($1 $0) (destination:<evt,<rel,<ent,t>>> $0 $3 $2))))))


move :- (((S/NP)/PP)/NP)/NP : (lambda $3:e (lambda $4:e (lambda $2:e (lambda $1:e (lambda $0:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $0 move:act $3) (destination:<evt,<rel,<ent,<me,t>>>> $0 $2 $1 $4)))))))

move :- ((S/PP)/NP)/NP : (lambda $3:e (lambda $2:e (lambda $1:e (lambda $0:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $0 move:act $3) (destination:<evt,<rel,<me,t>>> $0 $1 $2))))))


// Relation

left of :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 left:rel $2) ($1 $0)))))
right of :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 right:rel $2) ($1 $0)))))
closest to :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 nearest:rel $2) ($1 $0)))))
in :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 within:rel $2) ($1 $0)))))
at :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 within:rel $2) ($1 $0)))))
above :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 above:rel $2) ($1 $0)))))
on top of :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 above:rel $2) ($1 $0)))))
next to :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 adjacent:rel $2) ($1 $0)))))
in front of :- (N\N)/NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 front:rel $2) ($1 $0)))))

left of :- PP : left:rel
right of :- PP : right:rel
closest to :- PP : nearest:rel
in :- PP : within:rel
at :- PP : within:rel
above :- PP : above:rel
on top of :- PP : above:rel
next to :- PP : adjacent:rel
in front of :- PP : front:rel
forward :- PP : forward:rel

from the :- ((N\N)/NP)/PP : (lambda $2:e (lambda $3:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 $2 $3) ($1 $0))))))

that is :- ((N\N)/NP)/PP : (lambda $2:e (lambda $3:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 $2 $3) ($1 $0))))))

which is :- ((N\N)/NP)/PP : (lambda $2:e (lambda $3:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<ent,t>>> $0 $2 $3) ($1 $0))))))


forward :- (N\N)\NP : (lambda $2:e (lambda $1:<e,t> (lambda $0:e (and:<t*,t> (relation:<ent,<rel,<me,t>>> $0 forward:rel $2) ($1 $0)))))


// Sequence

and :- (S\S)/S : (lambda $0:<e,t> (lambda $1:<e,t> (sequence:<evt,<evt,t>> (det:<<e,t>,e> $1) (det:<<e,t>,e> $0))))


// Reference

[1] :- N\N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (id:<ent,<i,t>> $1 1:i) ($0 $1))))

it (1) :- NP : (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (type:<ent,<typ,t>> $1 reference:typ) (reference-id:<ent,<i,t>> $1 1:i))))

one {1} :- N : (lambda $1:e (and:<t*,t> (type:<ent,<typ,t>> $1 type-reference:typ) (reference-id:<ent,<i,t>> $1 1:i)))

ones {1} :- N : (lambda $1:e (and:<t*,t> (type:<ent,<typ,t>> $1 type-reference-group:typ) (reference-id:<ent,<i,t>> $1 1:i)))


// Cardinal

one :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (cardinal:<ent,<i,t>> $1 1:i) ($0 $1)))))
two :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (cardinal:<ent,<i,t>> $1 1:i) ($0 $1)))))

1 :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (cardinal:<ent,<i,t>> $1 1:i) ($0 $1)))))
2 :- NP/N : (lambda $0:<e,t> (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (cardinal:<ent,<i,t>> $1 1:i) ($0 $1)))))

