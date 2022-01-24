<?php

require_once('for_php7.php');

class knji110Form1
{
    function main(&$model)
    {
        //権限チェック
       if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
       }

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knji110index.php", "", "edit");

        //--------------最終学期の場合の制御    start----
        /* 最終学期の場合、次の年度の処理が行える */

        //最終学期かを判定
        $year_control = (trim(CTRL_SEMESTER) == trim($model->control["学期数"]))? true : false ;

        //最終学期ならコピー画面を表示しない。
        $arg["Show_control"] = (!$year_control);

        //年度コンボボックス
        $i = 0;
        $opt    = array();
        $db     = Query::dbCheckOut();
        $result = $db->query(knji110query::SelectYear($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTER"]."学期",
                           "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->cmd2 != ""){
            $opt[$i] = array("label" => SUBSTR($model->term,0,4)."年度  ".SUBSTR($model->term,5)."学期",
                             "value" => $model->term);
            $model->cmd2 = "";
        }

        if (!$model->term) {
            $model->term = $opt[0]["value"];
        }
        $extra = "onChange=\"btn_submit('list')\"";
        $arg["term"] = knjCreateCombo($objForm, "term", $model->term, $opt, $extra, 1);

        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の学期のデータをコピー", $extra);

        //年度追加
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //学期追加
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["seme_add"] = knjCreateTextBox($objForm, "", "seme_add", 1, 1, $extra);

        $extra = "onclick=\"return add('list');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        //参照年度コンボボックス
        $opt2 = array();
        $db     = Query::dbCheckOut();
        $result = $db->query(knji110query::SelectYear($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("label" => $row["YEAR"]."年度  ".$row["SEMESTER"]."学期",
                            "value" => $row["YEAR"]."-".$row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        $extra = "";
        $arg["term2"] = knjCreateCombo($objForm, "term2", $model->term2, $opt2, $extra, 1);

        $db = Query::dbCheckOut();

        $result = $db->query(knji110query::SelectList($model, $model->term));
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $row["URL"] = View::alink("knji110index.php", $row["HR_CLASS"], "target=right_frame",
                                       array("cmd"        => "edit",
                                             "GRADE"       => $row["GRADE"],
                                             "HR_CLASS"    => $row["HR_CLASS"],
                                             "term"        => $model->term));
            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";  //#ccffff
            
            //職員番号マスク処理
            for ($cdNm = 1; $cdNm <= 3; $cdNm++) {
                setTrcd("TR_CD{$cdNm}", $row, $model);
                setTrcd("SUBTR_CD{$cdNm}", $row, $model);
            }

            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "cmd2");

        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "list" && VARS::get("ed") != "1") {
            $arg["reload"] = "window.open('knji110index.php?cmd=edit&init=1','right_frame');";
        }

        View::toHTML($model, "knji110Form1.html", $arg);
    }
}
function setTrcd($trName, &$row, $model) {
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $ume = "" ;
    if ($row[$trName]) {
        for ($umecnt = 1; $umecnt <= strlen($row[$trName]) - (int)$simo; $umecnt++) {
            $ume .= $fuseji;
        }
        if ($fuseji) {
            $row[$trName] = $ume.substr($row[$trName], (strlen($row[$trName]) - (int)$simo), (int)$simo);
        }
    }
}
?>
