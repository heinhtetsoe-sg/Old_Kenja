{rval start}
<!-- kanji=漢字 -->
<!-- $Id: knjb0060Form1.html,v 1.5 2005/12/19 12:19:04 tamura Exp $ -->
<APPLET
    codebase = {rval APPHOST}
    CODE="jp.co.alp.kenja.hiro.knjb0060.KNJB0060.class"
    ARCHIVE="
            KNJB0060.jar,
            ../lib/kenja_applet.jar,
            ../lib/kenja_common.jar,
            ../lib/jp.gr.java_conf.tame.jar,
            ../lib/java_swing_misc.jar,
            ../lib/db2jcc4.jar,
            ../lib/db2jcc_license_cu.jar,
            ../lib/commons-beanutils-1.8.0.jar,
            ../lib/commons-collections-3.2.1.jar,
            ../lib/commons-dbcp-1.2.2.jar,
            ../lib/dbutils.jar,
            ../lib/commons-lang-2.4.jar,
            ../lib/commons-logging-1.1.1.jar,
            ../lib/commons-pool-1.4.jar,
            ../lib/jakarta-oro-2.0.8.jar,
            ../lib/log4j-1.2.15.jar"
    ALT="kenja-KNJB0060"
    WIDTH="100%"
    HEIGHT="90%">
    <param name="staffcd"	value="{rval staffcd}">
    <param name="year"	    value="{rval year}">
    <param name="semester"	value="{rval semester}">
    <param name="dbhost"	value="{rval dbhost}">
    <param name="dbname"	value="{rval dbname}">
    <param name="ctrl_m.ctrl_year"          value="{rval ctrl_m_ctrl_year}">
    <param name="ctrl_m.ctrl_semester"      value="{rval ctrl_m_ctrl_semester}">
    <param name="ctrl_m.ctrl_date"          value="{rval ctrl_m_ctrl_date}">
    <param name="ctrl_m.attend_ctrl_date"   value="{rval ctrl_m_attend_ctrl_date}">
</APPLET>
{rval finish}
