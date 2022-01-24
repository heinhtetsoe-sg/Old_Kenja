-- $Id: 180927038ed3cdf6e1cbb149f73a6af26678506f $
-- ���������б�(���������ɣ����б�)�ΰ٤δؿ�
-- �ʲ��Σ��Ĥ�UDF��������롣�ؿ�̾��Ʊ����
--	��varchar���Ϥ��� int �ʹ��������ɤ�����ؿ�
--	��int ���Ϥ��� varchar �ʹ��������ɤ�����ؿ�
--
-- ������ץȤλ�����ˡ: db2 -f <���Υե�����>

-- ǰ�ΰٺ�����ʤ���� DB21034E �� SQL0458N �Υ�å��������Ф롣
drop function f_period(int)
drop function f_period(varchar(1))

-- ����ι��������ɤ��֤���
-- '0'��'9'���Ϥ���0��9���֤���'A'��'Z'���Ϥ���10��35���֤���'a'��'z'���Ϥ���10��35���֤���
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

-- ����ι��������ɤ��֤���
-- 0��9���Ϥ���'0'��'9'���֤���10��35���Ϥ���'A'��'Z'���֤���'a'��'z'���֤����ȤϤʤ���
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
