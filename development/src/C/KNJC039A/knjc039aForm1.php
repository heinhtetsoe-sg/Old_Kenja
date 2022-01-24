<?php

require_once('for_php7.php');

class knjc039aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjc039aindex.php", "", "edit");
        $arg["YEAR"] = CTRL_YEAR;

        //前年度コピーボタン
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        $db = Query::dbCheckOut();

        //校種配列
        $schKindArr =array();
        $query = knjc039aQuery::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schKindArr[$row["VALUE"]] = $row["LABEL"];
       }
        $result->free();

        //一覧取得(ATTEND_REASON_COLLECTION_MST)
        $result = $db->query(knjc039aQuery::getList($model)); 
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            //リンク生成
            $aAdditional = " target=\"right_frame\" ";
            $aHash = array(
                "cmd"               => "edit",
                "YEAR"              => $row["YEAR"],
                "SCHOOL_KIND"       => $row["SCHOOL_KIND"],
                "COLLECTION_CD"     => $row["COLLECTION_CD"],
            );
            $row["ALINK"] = View::alink("knjc039aindex.php", $row["COLLECTION_CD"], $aAdditional, $aHash);

            //表示形式修正
            $row["SCHOOL_KIND"] = $row["SCHOOL_KIND"].":".$schKindArr[$row["SCHOOL_KIND"]];
            $row["FROM_DATE"]   = str_replace("-", "/", $row["FROM_DATE"]);
            $row["TO_DATE"]     = str_replace("-", "/", $row["TO_DATE"]);
            
            $arg["data"][] = $row;
        }

        $result->free();

        Query::dbCheckIn($db);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc039aForm1.html", $arg);
    }
}

?>
