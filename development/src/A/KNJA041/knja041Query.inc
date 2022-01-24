<?php

require_once('for_php7.php');

class knja041Query extends Query {
    
    function getSchoolName() {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学校マスタ取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //動作条件チェック
    function ChecktoStart()
    {
        return "SELECT COUNT(*) FROM V_COURSE_MAJOR_MST";
    }
    
    function UpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //UPDATE
        for ($i = 0; $i < get_count($model->data_arr); $i++)
        {
            $data["SCHREGNO"][TEXT]        = $model->data_arr[$i]["SCHREGNO"];
            $data["YEAR"][TEXT]            = $model->data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]        = $model->data_arr[$i]["SEMESTER"];
            $data["GRADE"][TEXT]           = $model->data_arr[$i]["GRADE"];
            $data["HR_CLASS"][TEXT]        = $model->data_arr[$i]["HR_CLASS"];
            $data["ATTENDNO"][TEXT]        = $model->data_arr[$i]["ATTENDNO"];
            $data["COURSECD"][TEXT]        = $model->data_arr[$i]["COURSECD"];
            $data["MAJORCD"][TEXT]         = sprintf("%03d",$model->data_arr[$i]["MAJORCD"]);
            $data["COURSECODE"][TEXT]      = sprintf("%04d",$model->data_arr[$i]["COURSECODE"]);
            $data["REMAINGRADE_FLG"][TEXT] = $model->data_arr[$i]["REMAINGRADE_FLG"];
            $data["OLD_SCHREGNO"][TEXT]    = $model->data_arr[$i]["OLD_SCHREGNO"];
            $data["OLD_GRADE"][TEXT]       = $model->data_arr[$i]["OLD_GRADE"];
            $data["OLD_HR_CLASS"][TEXT]    = $model->data_arr[$i]["OLD_HR_CLASS"];
            $data["OLD_ATTENDNO"][TEXT]    = $model->data_arr[$i]["OLD_ATTENDNO"];
            $data["SCORE"][FUNC]           = $model->data_arr[$i]["SCORE"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "SYSDATE()";

			$where  ="WHERE SCHREGNO = '".$model->data_arr[$i]["SCHREGNO"]."'";
			$where .="      AND YEAR = '".$model->data_arr[$i]["YEAR"]."'";
			$where .="      AND SEMESTER = '".$model->data_arr[$i]["SEMESTER"]."'";

			$query = Query::updateSQL($data,"CLASS_FORMATION_DAT",$where);
            $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //処理年度以前の在籍データに同じ学籍番号があるかチェックする用
    function SchregnoExist($model,$schregno,$db)
    {

        //CLASS_FORMATION_DATをチェック
        $query  = "SELECT SCHREGNO FROM CLASS_FORMATION_DAT ";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $query .= "   AND YEAR     = '".$model->new_year."'";
        $query .= "   AND SEMESTER  = '1'";
        $ret_val1 = $db->getOne($query);

        if(isset($ret_val1))
        {
            return true;
        }else {
            return false;
		}
    }
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {

		$data1["MSGROW"][NUMBER] 		= $record_no;
		$data1["MSGREMARK"][TEXT] 		= $check_error;
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
		$query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }
} 
?>
