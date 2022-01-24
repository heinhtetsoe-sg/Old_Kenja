<?php

require_once('for_php7.php');

class knjh020_2Form1
{

    function main(&$model)
    {
        //ヘッダー部作成
        $Row_himself = knjh020_2Query::getRow_himself($model);

        if($model->schregno){
            //表示-----
            //学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = $Row_himself["NAME_SHOW"];
            //生年月日
            $birth_day = array();
            $birth_day = explode("-",$Row_himself["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
        }else{
            //非表示-----
            //学籍番号
            $arg["header"]["SCHREGNO"] = "　　　　";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "　　";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "　　年　　月　　日";
        }

        $db = Query::dbCheckOut();
        $query = "SELECT * FROM V_NAME_MST WHERE YEAR='".CTRL_YEAR."' AND NAMECD1 IN ('Z002','H201','H200') ";	//NO002
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $Name[$row["NAMECD1"]][$row["NAMECD2"]] = $row["NAME1"];
        }
        //SQL文発行
        $query = knjh020_2Query::getRow_relative_list($model);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
               $row["RELANO"] = View::alink("knjh020_2index.php", $row["RELANO"],"target=\"bottom_frame\"",
               array("cmd"         => "edit",
                     "SCHREGNO"    => $row["SCHREGNO"],
                     "RELANO"      => $row["RELANO"]));

               $row["RELASEX"] = $Name['Z002'][$row["RELASEX"]];;
               $row["RELATIONSHIP"] = $Name['H201'][$row["RELATIONSHIP"]];
               $row["REGIDENTIALCD"] = $Name['H200'][$row["REGIDENTIALCD"]];	//NO002
               //レコードを連想配列のまま配列$arg[data]に追加していく。
               $arg["data"][] = $row;
        }
        $arg["SCHREGNO"]    = $model->schregno;
        $arg["NAME"]        = $model->name;

        Query::dbCheckIn($db);
       if (VARS::get("SCHREGNO") || VARS::get("init") == 1){
               $arg["reload"] = "window.open('knjh020_2index.php?cmd=edit','edit_frame');";
       }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh020_2Form1.html", $arg);
    }
}
?>
