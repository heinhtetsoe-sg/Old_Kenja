<?php

require_once('for_php7.php');

class knje100query extends Query {
    //模試コード取得
    function selectGradeQuery($model)
    {
        $query  = "SELECT NAMECD2,NAME1 ";
        $query .= "  FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '" .$model->control["年度"] ."'";
        $query .= "   AND NAMECD1 = 'D300'";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //模試データの削除
    function deleteQueryCsv($data)
    {
        $query  = "DELETE FROM SHAMEXAMINATION_DAT ";
        $query .= " WHERE YEAR 		= '" .$data["YEAR"] ."'";
        $query .= " AND SHAMEXAMCD 	= '" .sprintf("%02d", $data["SHAMEXAMCD"]) ."'";
        $query .= " AND SCHREGNO 	= '" .$data["SCHREGNO"] ."'";
        $query .= " AND SUBCLASSCD 	= '" .sprintf("%04d", $data["SUBCLASSCD"]) ."'";

        return $query;
    }

    //模試データの作成（ＣＳＶデータより読込）
    function insertQueryCsv($data)
    {
        $query  = "INSERT INTO SHAMEXAMINATION_DAT ";
        $query .= "            (";
        $query .= "              YEAR";
        $query .= "             ,SHAMEXAMCD";
        $query .= "             ,SCHREGNO";
        $query .= "             ,CLASSCD";
        $query .= "             ,SCHOOL_KIND";
        $query .= "             ,CURRICULUM_CD";
        $query .= "             ,SUBCLASSCD";
        $query .= "             ,SUBCLASSNAME";
        $query .= "             ,SCORE";
        $query .= "             ,DEVIATION";
        $query .= "             ,UPDATED";
        $query .= "            )";
        $query .= "     VALUES";
        $query .= "            (";
        $query .= "              '" .$data["YEAR"] ."'";
        $query .= "             ,'" .sprintf("%02d", $data["SHAMEXAMCD"]) ."'";
        $query .= "             ,'" .$data["SCHREGNO"] ."'";
        $query .= "             ,'99'";
        $query .= "             ,'J'";
        $query .= "             ,'9'";
        $query .= "             ,'" .sprintf("%04d", $data["SUBCLASSCD"]) ."'";
        $query .= "             ,'" .$data["SUBCLASSNAME"] ."'";
        $query .= "             ," .$data["SCORE"];
        $query .= "             ," .$data["DEVIATION"];
        $query .= "             ,SYSDATE()";
        $query .= "            )";

        return $query;
    }

    //科目マスタの存在チェック
    function checkQuerySub($data)
    {
        $query  = "SELECT COUNT(*) FROM SUBCLASS_MST";
        $query .= " WHERE SUBCLASSCD = '" .sprintf("%04d", $data["SUBCLASSCD"]) ."'";

        return $query;
    }

    //学籍在籍データの存在チェック
    function checkQuerySch($data)
    {
        $query  = "SELECT COUNT(*) FROM SCHREG_REGD_DAT";
        $query .= " WHERE SCHREGNO 	= '" .$data["SCHREGNO"] ."'";
        $query .= " AND YEAR 		= '" .$data["YEAR"] ."'";

        return $query;
    }
}
?>
