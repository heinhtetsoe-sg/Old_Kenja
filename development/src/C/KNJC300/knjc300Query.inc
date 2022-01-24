<?php

require_once('for_php7.php');
class knjc300Query extends Query {
    
    //発信者取得
    function getStaff()
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        //$query .= "     STAFFCD = '".$staff."' ";
        
        //$result = $db->query($query);
        //$row    = $result -> fetchRow(DB_FETCHMODE_ASSOC);
        //return $row["STAFFNAME"];
        $query .= " ORDER BY VALUE ";
        return $query;
    }
    
    //有効日付取得
    function getDays()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS FROM, ";
        $query .= "     NAME1 AS TO ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'C030' ";
        //$query .= " ORDER BY VALUE ";
        //$query  = " SELECT ";
        //$query .= "     HREFFCD2 AS VALUE, ";
        //$query .= "     HREFFNAME AS LABEL ";
        //$query .= " FROM ";
        //$query .= "     H_REFF ";
        //$query .= " WHERE ";
        //$query .= "     HREFFCD1 = '9000' ";
        //$query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //所属取得
    function getSelectCourse()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD AS VALUE, ";
        $query .= "     COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //ＨＲクラス取得
    function getHRclass()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || ':' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //科目取得
    function getKamoku($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";
        //$query .= "     T2.YEAR = '2005' AND ";
        //$query .= "     T2.SEMESTER = '1' ";
        $query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //講座取得
    function getKouza($model, $SUBCLASSCD)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$SUBCLASSCD."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$SUBCLASSCD."' AND ";
        }
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        //$query .= "     YEAR = '2005' AND ";
        //$query .= "     SEMESTER = '1' ";
        $query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //クラブ／委員会取得
    function getClubCommittee()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     'CL:' || 'X:' || T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBNAME AS LABEL, ";
        $query .= "     T2.YEAR ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = T2.CLUBCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        //$query .= "     T2.YEAR = '2005' ";
        
        $query .= " UNION ";
        
        $query .= " SELECT DISTINCT ";
        $query .= "     'CO:' || T1.COMMITTEE_FLG || ':' || T1.COMMITTEECD AS VALUE, ";
        $query .= "     T1.COMMITTEENAME AS LABEL, ";
        $query .= "     T2.YEAR ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_MST T1, ";
        $query .= "     COMMITTEE_YDAT T2 ";
        $query .= " WHERE ";
        //$query .= "     T1.COMMITTEECD = T2.COMMITTEENAME AND ";
        $query .= "     T1.COMMITTEECD = T2.COMMITTEECD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        //$query .= "     T2.YEAR = '2005' ";
        
        $query .= " ORDER BY VALUE ";
        
        return $query;
    }
    
    //生徒一覧の取得　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
    function getStudent($course,$select,$homeroom,$kamoku,$kouza,$clco,$model)
    {
        
        list($grade,$class) = explode(':',$homeroom);
        list($kubun,$flg,$code)  = explode(':',$clco);
        
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || T2.NAME AS LABEL ";
        //科目選択された場合はこの項目も取得
        if ($kamoku != ""){
            $query .= ", T3.CHAIRCD ";
        }
        //クラブ／委員会が選択された場合はこの項目も取得
        //クラブ
        if ($kubun == "CL"){
            $query .= ", T3.CLUBCD ";
        }
        //委員会
        if ($kubun == "CO"){
            $query .= ", T3.COMMITTEECD ";
        }
        
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        //科目選択された場合はこのテーブルも結合
        if ($kamoku != ""){
            $query .= ", CHAIR_STD_DAT T3 ";
        }
        //クラブ／委員会が選択された場合はこのテーブルも結合
        //クラブ
        if ($kubun == "CL"){
            $query .= ", SCHREG_CLUB_HIST_DAT T3 ";
        }
        //委員会
        if ($kubun == "CO"){
            $query .= ", SCHREG_COMMITTEE_HIST_DAT T3 ";
        }
        
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        //科目選択された場合は結合条件を追加
        if ($kamoku != ""){
            $query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
            $query .= "     AND T1.YEAR     = T3.YEAR ";
            $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
            //$query .= "     AND '".$model->senddate."' BETWEEN T3.APPDATE AND T3.APPENDDATE ";    //システム日付
            $query .= "     AND '".CTRL_DATE."' BETWEEN T3.APPDATE AND T3.APPENDDATE ";             //ログオン日付
            //講座選択された場合はひとつのみ
            if ($kouza != ""){
                $query .= "     AND T3.CHAIRCD = '".$kouza."' ";
            } else {
                $query .= "     AND T3.CHAIRCD IN ('" .str_replace("," ,"','" ,$model->kamoku) ."') ";
            }
        }
        //クラブ／委員会が選択された場合は結合条件を追加
        if ($kubun != ""){
            $query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
            //クラブ
            if ($kubun == "CL"){
                $query .= "     AND T3.CLUBCD = '".$code."' ";
            }
            //委員会
            if ($kubun == "CO"){
                $query .= "     AND T3.COMMITTEE_FLG = '".$flg."' ";
                $query .= "     AND T3.COMMITTEECD = '".$code."' ";
                //$query .= "     AND T3.YEAR = '2005' ";
                $query .= "     AND T3.YEAR = '".CTRL_YEAR."' ";
            }
        }
        //生徒選択後選択生徒を除いて表示
        if ($select == "1"){
            $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata) ."') ";
        }
        //生徒選択後選択済み生徒を表示
        if ($select == "2"){
            $query .= "     AND T1.SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata) ."') ";
        }
        if($course != 0){
            $query .= " AND T1.COURSECD = '".$course."' ";
        }
        if($homeroom != ""){
            $query .= " AND T1.GRADE = '".$grade."' ";
            $query .= " AND T1.HR_CLASS = '".$class."' ";
        }
        
        $query .= " ORDER BY VALUE ";
        
        //echo "query=".$query."<BR>";
        
        return $query;
    }
    
    //伝言データの追加　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
    
    //伝言ファイル管理番号最大取得
    function getMessageNo($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(MESSAGENO) ";
        $query .= " FROM ";
        $query .= "     DENGON ";
        
        return $query;
    }
    
    //伝言ファイル更新
    function getDengonInsQuery($db, $model,$val)
    {
        
        $MESSAGENO = $db->getOne(knjc300Query::getMessageNo($model));
        $MESSAGENO = $MESSAGENO == "" ? "1" : sprintf("%d", $MESSAGENO + 1);
        
        $data["MESSAGENO"][NUMBER]  = $MESSAGENO;
        $data["SCHREGNO"][TEXT]     = $val;
        $data["STAFFCD"][TEXT]      = $model->tanto;
        $data["TERMDATE"][TEXT]     = $model->termdate;
        $data["SENDDATE"][TEXT]     = $model->senddate;
        $data["MESSAGE1"][TEXT]     = $model->message1;
        $data["MESSAGE2"][TEXT]     = $model->message2;
        
        $query = Query::insertSQL($data, "DENGON");
        
        return $query;
    }
    
}
?>
