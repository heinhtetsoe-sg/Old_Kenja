-- $Id: 180927038ed3cdf6e1cbb149f73a6af26678506f $
-- １６校時対応(校時コード２桁対応)の為の関数
-- 以下の２つのUDFを作成する。関数名は同じ。
--	・varcharを渡して int な校時コードを得る関数
--	・int を渡して varchar な校時コードを得る関数
--
-- スクリプトの使用方法: db2 -f <このファイル>

-- 念の為削除。なければ DB21034E と SQL0458N のメッセージが出る。
drop function f_period(int)
drop function f_period(varchar(1))

-- ２桁の校時コードを返す。
-- '0'〜'9'を渡すと0〜9を返す。'A'〜'Z'を渡すと10〜35を返す。'a'〜'z'を渡すと10〜35を返す。
create function f_period(pcd varchar(1)) \
returns int \
LANGUAGE SQL \
CONTAINS SQL \
NO EXTERNAL ACTION \
DETERMINISTIC \
return \
  case \
    when pcd between 'a' and 'z' then 10+(ascii(pcd) - ascii('a')) \
    when pcd between 'A' and 'Z' then 10+(ascii(pcd) - ascii('A')) \
    when pcd between '0' and '9' then 0+(ascii(pcd) - ascii('0')) \
  end

-- １桁の校時コードを返す。
-- 0〜9を渡すと'0'〜'9'を返す。10〜35を渡すと'A'〜'Z'を返す。'a'〜'z'を返すことはない。
create function f_period(pcd int) \
returns varchar(1) \
LANGUAGE SQL \
CONTAINS SQL \
NO EXTERNAL ACTION \
DETERMINISTIC \
return \
  case \
    when pcd between 10 and 35 then chr(pcd+(ascii('A')-10)) \
    when pcd between  0 and  9 then chr(pcd+ascii('0')) \
  end
