<?php

require_once('for_php7.php');

class knjb3050Query extends Query {

    //CHAIR_LC_DATにデータがあるかどうか。
    function getMainDataHeadLCCount($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_LC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$model->getParam['year']}'";
        $query .= "     AND T1.SEMESTER='{$model->getParam['semester']}'";
        if($model->getParam['groupcd'] == '0000'){
            $query .= "     AND T1.CHAIRCD = '{$model->getParam['chaircd']}' ";
        } else {
            $query .= "     AND T1.GROUPCD = '{$model->getParam['groupcd']}' ";
        }
        
        return $query;
    }
    
    //上行のヘッダ情報を取得
    function getMainDataHead($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD as SUBCLASSCD, ";
        $query .= "     V1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     v_subclass_mst V1 ";
        $query .= "     ON V1.YEAR = T1.YEAR ";
        $query .= "     AND V1.CLASSCD = T1.CLASSCD ";
        $query .= "     AND V1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     AND V1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND V1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND T1.SEMESTER='{$model->getParam['semester']}' ";
        if($model->getParam['groupcd'] == '0000'){
            $query .= "     AND T1.CHAIRCD = '{$model->getParam['chaircd']}' ";
        } else {
            $query .= "     AND T1.GROUPCD = '{$model->getParam['groupcd']}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    //上行の職員名を取得
    function getMainDataHeadSTAFF($model, $chaircd) {
        $query .= " SELECT ";
        $query .= "     CSTF.STAFFCD, ";
        $query .= "     M1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT CSTF ";
        $query .= " INNER JOIN ";
        $query .= "     STAFF_MST M1 ";
        $query .= "     ON CSTF.STAFFCD = M1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     CSTF.CHARGEDIV = 1 ";
        $query .= "     AND CSTF.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND CSTF.SEMESTER='{$model->getParam['semester']}' ";
        $query .= "     AND CSTF.CHAIRCD = '{$chaircd}' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD DESC ";
        
        return $query;
    }

    //上行のリスト情報を取得
    function getMainData($model, $chairCd, $div = '') {
        $query .= " SELECT ";
        $query .= "     VALUE(M2.NAME2, '　') || ' ' || H1.HR_NAMEABBV || '-' || D1.ATTENDNO || ':' || M1.NAME AS LABEL, ";
        $query .= "     D1.SCHREGNO AS VALUE, ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.HR_CLASS, ";
        $query .= "     D1.ATTENDNO, ";
        $query .= "     D1.GRADE || '-' || D1.HR_CLASS AS PROP, ";
        $query .= "     D1.GRADE || '-' || D1.HR_CLASS  || '-' || D1.ATTENDNO AS PROP2 ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1  ";
        $query .= " INNER JOIN  ";
        $query .= "     SEMESTER_MST S1  ";
        $query .= "     ON S1.YEAR = T1.YEAR  ";
        $query .= "     AND S1.SEMESTER = T1.SEMESTER  ";
        $query .= "     AND S1.EDATE = T1.APPENDDATE ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT D1 ";
        $query .= "     ON D1.YEAR = S1.YEAR ";
        $query .= "     AND D1.SEMESTER = S1.SEMESTER ";
        $query .= "     AND D1.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= "     ON H1.YEAR = S1.YEAR ";
        $query .= "     AND H1.SEMESTER = S1.SEMESTER ";
        $query .= "     AND H1.GRADE = D1.GRADE ";
        $query .= "     AND H1.HR_CLASS = D1.HR_CLASS ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_BASE_MST M1 ";
        $query .= "     ON M1.SCHREGNO = D1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST M2 ";
        $query .= "     ON M2.NAMECD1 = 'Z002' ";
        $query .= "     AND M2.NAMECD2 = M1.SEX ";
        $query .= " WHERE ";
        $query .= "     S1.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND S1.SEMESTER='{$model->getParam['semester']}' ";
        if ($div == 'GEDAN') {
            $query .= "     AND T1.CHAIRCD IN ('". implode("','", $model->chairJoudan)."') ";
        } else {
            $query .= "     AND T1.CHAIRCD = '{$chairCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.HR_CLASS, ";
        $query .= "     D1.ATTENDNO ";
        
        return $query;
    }
    
    //上行のリスト情報のLC版を取得
    function getMainDataLC($model, $chairCd, $div = '') {
        $query .= " SELECT ";
        $query .= "     VALUE(M2.NAME2, '　') || ' ' || H1.LC_NAMEABBV || '-' || VALUE(D1.ATTENDNO, '') || ':' || M1.NAME AS LABEL, ";
        $query .= "     D1.SCHREGNO AS VALUE, ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.LC_CLASS, ";
        $query .= "     D1.ATTENDNO, ";
        $query .= "     D1.GRADE || '-' || D1.LC_CLASS AS PROP, ";
        $query .= "     D1.GRADE || '-' || D1.LC_CLASS  || '-' || D1.ATTENDNO AS PROP2 ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1  ";
        $query .= " INNER JOIN SEMESTER_MST S1  ";
        $query .= "     ON S1.YEAR = T1.YEAR  ";
        $query .= "     AND S1.SEMESTER = T1.SEMESTER  ";
        $query .= "     AND S1.EDATE = T1.APPENDDATE  ";
        $query .= " INNER JOIN SCHREG_REGD_LC_DAT D1  ";
        $query .= "     ON D1.YEAR = S1.YEAR  ";
        $query .= "     AND D1.SEMESTER = S1.SEMESTER  ";
        $query .= "     AND D1.SCHREGNO = T1.SCHREGNO  ";
        $query .= " INNER JOIN SCHREG_REGD_LC_HDAT H1  ";
        $query .= "     ON H1.YEAR = S1.YEAR  ";
        $query .= "     AND H1.SEMESTER = S1.SEMESTER  ";
        $query .= "     AND H1.GRADE = D1.GRADE  ";
        $query .= "     AND H1.LC_CLASS = D1.LC_CLASS  ";
        $query .= " INNER JOIN SCHREG_BASE_MST M1  ";
        $query .= "     ON M1.SCHREGNO = D1.SCHREGNO  ";
        $query .= " LEFT JOIN NAME_MST M2  ";
        $query .= "     ON M2.NAMECD1 = 'Z002'  ";
        $query .= "     AND M2.NAMECD2 = M1.SEX ";
        $query .= " WHERE ";
        $query .= "     S1.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND S1.SEMESTER='{$model->getParam['semester']}' ";
        if ($div == 'GEDAN') {
            $query .= "     AND T1.CHAIRCD IN ('". implode("','", $model->chairJoudan)."') ";
        } else {
            $query .= "     AND T1.CHAIRCD = '{$chairCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.LC_CLASS, ";
        $query .= "     D1.ATTENDNO ";
        
        return $query;
    }
    
    //下行のヘッダ情報を取得
    function getMainData2Head($model) {
        $query .= " SELECT ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     CLS.CHAIRCD, ";
        $query .= "     CLS.TRGTGRADE, ";
        $query .= "     CLS.TRGTCLASS, ";
        $query .= "     HDAT.HR_NAMEABBV AS NAMEABBV,  ";
        $query .= "     CLS.TRGTGRADE || '-' || CLS.TRGTCLASS AS PROP  ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CLS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     ON HDAT.YEAR = CLS.YEAR ";
        $query .= "     AND HDAT.SEMESTER = CLS.SEMESTER ";
        $query .= "     AND HDAT.GRADE = CLS.TRGTGRADE ";
        $query .= "     AND HDAT.HR_CLASS = CLS.TRGTCLASS ";
        $query .= " WHERE ";
        $query .= "     CLS.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND CLS.SEMESTER='{$model->getParam['semester']}' ";
        if($model->getParam['groupcd'] == '0000'){
            $query .= "     AND CLS.CHAIRCD = '{$model->getParam['chaircd']}' ";
        } else {
            $query .= "     AND CLS.GROUPCD = '{$model->getParam['groupcd']}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLS.CHAIRCD ";
        return $query;
    }

    //下行のヘッダ情報のLC版を取得
    function getMainData2HeadLC($model) {
        $query .= " SELECT ";
        $query .= "     HDAT.LC_NAME AS HR_NAME, ";
        $query .= "     CLS.CHAIRCD, ";
        $query .= "     CLS.LCGRADE AS TRGTGRADE, ";
        $query .= "     CLS.LCCLASS AS TRGTCLASS,  ";
        $query .= "     HDAT.LC_NAMEABBV AS NAMEABBV,  ";
        $query .= "     CLS.LCGRADE || '-' || CLS.LCCLASS AS PROP  ";
        $query .= " FROM  ";
        $query .= "     CHAIR_LC_DAT CLS  ";
        $query .= " LEFT JOIN SCHREG_REGD_LC_HDAT HDAT  ";
        $query .= "     ON HDAT.YEAR = CLS.YEAR  ";
        $query .= "     AND HDAT.SEMESTER = CLS.SEMESTER  ";
        $query .= "     AND HDAT.GRADE = CLS.LCGRADE  ";
        $query .= "     AND HDAT.LC_CLASS = CLS.LCCLASS ";
        $query .= " WHERE ";
        $query .= "     CLS.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND CLS.SEMESTER='{$model->getParam['semester']}' ";
        if($model->getParam['groupcd'] == '0000'){
            $query .= "     AND CLS.CHAIRCD = '{$model->getParam['chaircd']}' ";
        } else {
            $query .= "     AND CLS.GROUPCD = '{$model->getParam['groupcd']}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLS.CHAIRCD ";
        
        return $query;
    }

    //下行のリスト情報を取得
    function getMainData2($model, $chairCd, $grade, $hrClass) {
        $query  = " WITH JOUDAN_SQL AS ( ".knjb3050Query::getMainData($model, $chairCd, "GEDAN").") ";
        $query .= " SELECT ";
        $query .= "     VALUE(M2.NAME2, '　') || ' ' || H1.HR_NAMEABBV || '-' || D1.ATTENDNO || ':' || M1.NAME AS LABEL, ";
        $query .= "     D1.SCHREGNO AS VALUE, ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.HR_CLASS, ";
        $query .= "     D1.ATTENDNO, ";
        $query .= "     D1.GRADE || '-' || D1.HR_CLASS AS PROP, ";
        $query .= "     D1.GRADE || '-' || D1.HR_CLASS  || '-' || D1.ATTENDNO AS PROP2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT D1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= "     ON H1.YEAR = D1.YEAR ";
        $query .= "     AND H1.SEMESTER = D1.SEMESTER ";
        $query .= "     AND H1.GRADE = D1.GRADE ";
        $query .= "     AND H1.HR_CLASS = D1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST M1 ";
        $query .= "     ON M1.SCHREGNO = D1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST M2 ";
        $query .= "     ON M2.NAMECD1 = 'Z002' ";
        $query .= "     AND M2.NAMECD2 = M1.SEX ";
        $query .= " WHERE ";
        $query .= "     D1.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND D1.SEMESTER='{$model->getParam['semester']}' ";
        $query .= "     AND D1.GRADE='{$grade}' ";
        $query .= "     AND D1.HR_CLASS='{$hrClass}' ";
        $query .= "     AND D1.SCHREGNO NOT IN (SELECT JOUDAN_SQL.VALUE FROM JOUDAN_SQL) ";
        
        $query .= " ORDER BY ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.HR_CLASS, ";
        $query .= "     D1.ATTENDNO ";
        
        return $query;
    }
    
    //下行のリスト情報のLC版を取得
    function getMainData2LC($model, $chairCd, $grade, $hrClass) {
        $query  = " WITH JOUDAN_SQL AS ( ".knjb3050Query::getMainDataLC($model, $chairCd, "GEDAN").") ";
        $query .= " SELECT ";
        $query .= "     VALUE(M2.NAME2, '　') || ' ' || H1.LC_NAMEABBV || '-' || VALUE(D1.ATTENDNO, '') || ':' || M1.NAME AS LABEL, ";
        $query .= "     D1.SCHREGNO AS VALUE, ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.LC_CLASS, ";
        $query .= "     D1.ATTENDNO, ";
        $query .= "     D1.GRADE || '-' || D1.LC_CLASS AS PROP, ";
        $query .= "     D1.GRADE || '-' || D1.LC_CLASS || '-' || D1.ATTENDNO AS PROP2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_DAT D1  ";
        $query .= " LEFT JOIN SCHREG_REGD_LC_HDAT H1  ";
        $query .= "     ON H1.YEAR = D1.YEAR  ";
        $query .= "     AND H1.SEMESTER = D1.SEMESTER  ";
        $query .= "     AND H1.GRADE = D1.GRADE  ";
        $query .= "     AND H1.LC_CLASS = D1.LC_CLASS  ";
        $query .= " LEFT JOIN SCHREG_BASE_MST M1  ";
        $query .= "     ON M1.SCHREGNO = D1.SCHREGNO  ";
        $query .= " LEFT JOIN NAME_MST M2  ";
        $query .= "     ON M2.NAMECD1 = 'Z002'  ";
        $query .= "     AND M2.NAMECD2 = M1.SEX ";
        $query .= " WHERE ";
        $query .= "     D1.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND D1.SEMESTER='{$model->getParam['semester']}' ";
        $query .= "     AND D1.GRADE='{$grade}' ";
        $query .= "     AND D1.LC_CLASS='{$hrClass}' ";
        $query .= "     AND D1.SCHREGNO NOT IN (SELECT JOUDAN_SQL.VALUE FROM JOUDAN_SQL) ";
        $query .= " ORDER BY ";
        $query .= "     D1.GRADE, ";
        $query .= "     D1.LC_CLASS, ";
        $query .= "     D1.ATTENDNO ";
        
        return $query;
    }
    
    //日付の最大値を取得
    function maxDate($db, $model){

        $chaircds = "('' ";
        $result = $db->query(knjb3050Query::getMainDataHead($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chaircds .= ", '".$row["VALUE"]."' ";
        }
        $chaircds .= ")";

        $query .= " SELECT ";
        $query .= "     MAX(T1.EXECUTEDATE) ";
        $query .= " FROM ( ";
        $query .= "     SELECT ";
        $query .= "         SCH_CHR.EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT SCH_CHR ";
        $query .= "     WHERE ";
        $query .= "         SCH_CHR.EXECUTED='1' ";
        $query .= "         AND SCH_CHR.CHAIRCD IN ".$chaircds." ";
        $query .= "         AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SEMESTER_MST E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR = '{$model->getParam['year']}' ";
        $query .= "                 AND E1.SEMESTER = '{$model->getParam['semester']}' ";
        $query .= "                 AND SCH_CHR.EXECUTEDATE BETWEEN E1.SDATE AND E1.EDATE) ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         HRATE.EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_HRATE_DAT HRATE ";
        $query .= "     WHERE ";
        $query .= "         HRATE.EXECUTED='1' ";
        $query .= "         AND HRATE.CHAIRCD IN ".$chaircds." ";
        $query .= "         AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SEMESTER_MST E1 ";
        $query .= "             WHERE ";
        $query .= "                 E1.YEAR = '{$model->getParam['year']}' ";
        $query .= "                 AND E1.SEMESTER = '{$model->getParam['semester']}' ";
        $query .= "                 AND HRATE.EXECUTEDATE BETWEEN E1.SDATE AND E1.EDATE ";
        $query .= "         )) T1 ";
        
        return $query;
    }
    
    //学期の日付範囲を取得
    function getSemesterRange($model){
        $query .= " SELECT ";
        $query .= "     SMST.SDATE, ";
        $query .= "     SMST.EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SMST";
        $query .= " WHERE ";
        $query .= "     SMST.YEAR='{$model->getParam['year']}' ";
        $query .= "     AND SMST.SEMESTER='{$model->getParam['semester']}' ";
        
        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $newDate = date('Y-m-d',strtotime($model->date)-60*60*24);

        $result = $db->query(knjb3050Query::getMainDataHead($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $query  = " DELETE FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->getParam['year']}' ";
            $query .= " AND T1.SEMESTER     = '{$model->getParam['semester']}' ";
            $query .= " AND T1.APPDATE      = '" . str_replace('/', '-', $model->date) . "' ";
            $query .= " AND T1.CHAIRCD      = '{$row['VALUE']}' ";
            $db->query($query);

            $data = array();
            $data["APPENDDATE"][TEXT]        = $newDate;

            $where  = " WHERE ";
            $where .= "     YEAR         = '{$model->getParam['year']}' ";
            $where .= " AND SEMESTER     = '{$model->getParam['semester']}' ";
            $where .= " AND APPENDDATE   >= '" . str_replace('/', '-', $model->date) . "' ";
            $where .= " AND CHAIRCD      = '{$row['VALUE']}' ";

            $db->query(Query::updateSQL($data, "CHAIR_STD_DAT", $where));
            
        }
        
        $row = $db->getRow(knjb3050Query::getSemesterRange($model));
        $endDate = $row[1];
        $result = $db->query(knjb3050Query::getMainDataHead($model));
        $chairList = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairList[] = $row['VALUE'];
        }

        
        for($i=0;$i<get_count($model->selectdata);$i++){
            for($j=0;$j<get_count($model->selectdata[$i]);$j++){
                $data = array();
                $data["YEAR"][TEXT]           = $model->getParam['year'];
                $data["SEMESTER"][TEXT]       = $model->getParam['semester'];
                $data["CHAIRCD"][TEXT]        = $chairList[$i];
                $data["SCHREGNO"][TEXT]       = $model->selectdata[$i][$j];
                $data["APPDATE"][TEXT]        = str_replace('/', '-', $model->date);
                $data["APPENDDATE"][TEXT]     = $endDate;
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "SYSDATE()";
                
                $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                $db->query($query);
            }
        }
        
        Query::dbCheckIn($db);
    }
}
?>
