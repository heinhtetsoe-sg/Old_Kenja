drop function sysdate()

create function SYSDATE() \
returns Timestamp \
specific SYSDATEORACLE \ 
language sql \
contains sql \
no external action \ 
not deterministic \
return \
current timestamp \

