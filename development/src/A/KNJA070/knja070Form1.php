<?php

require_once('for_php7.php');

class knja070Form1
{
    public function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form();
        $arg["start"] = $objForm->get_start("list", "POST", "knja070index.php", "", "edit");

        $db     = Query::dbCheckOut();

        //学期コンボ
        $query = knja070Query::getSemester();
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "term", $model->term, $extra, 1);

        // SCHREG_REGD_GDATをチェックする
        $isError = knja070Query::checkRegdGdat($db, $model, $model->term);
        $disabled = "";
        if ($isError) {
            $disabled = " disabled ";
            $model->setMessage("MSG305", "指定年度の学年名称データを設定してください。");
        }

        $objForm->ae(array("type"      =>    "button",
                            "name"      =>    "btn_copy",
                            "value"     =>    "左の学期のデータをコピー",
                            "extrahtml" =>    "onClick=\"return btn_submit('copy');\"".$disabled));
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        $opt2 = array();
        //参照年度コンボボックス
        for ($i = 0; $i < $model->control["学期数"]; $i++) {
            $opt2[$i] = array("label" => CTRL_YEAR."年度  ".$model->control["学期名"][$i+1],
                              "value" => CTRL_YEAR."-".($i+1));
        }
        if (!isset($model->term2)) {
            $model->term2 = CTRL_YEAR . "-" . CTRL_SEMESTER;
        }
        $objForm->ae(array("type"      => "select",
                            "name"      => "term2",
                            "size"      => "1",
                            "value"     => $model->term2,
                            "options"   => $opt2));
        $arg["term2"] = $objForm->ge("term2");

        //生徒項目名切替処理
        $arg["SCH_LABEL"] = $model->sch_label;

        //生徒もコピーチェックボックス
        $extra  = ($model->check == "1" || $model->defFlg == "on") ? "checked" : "";
        $extra .= " id=\"check\"";
        $extra .= (substr($model->term, 0, 4) == substr($model->term2, 0, 4)) ? "" : " disabled";
        $extra .= " onclick=\"OptionUse('this');\"";
        $arg["btn_check"] = knjCreateCheckBox($objForm, "check", "1", $extra, "");

        //除籍者も含むチェックボックス
        $extra  = ($model->grd_div == "1" || $model->defFlg == "on") ? "checked" : "";
        $extra .= " id=\"grd_div\"";
        $extra .= ($model->check == "1" || $model->defFlg == "on") ? "" : " disabled";
        $arg["btn_grddiv"] = knjCreateCheckBox($objForm, "grd_div", "1", $extra, "");

        $model->defFlg = "off";

        //ＨＲクラス一覧
        $result = $db->query(knja070query::selectList($model, $model->term));
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["URL"] = View::alink(
                "knja070index.php",
                $row["HR_CLASS"],
                "target=right_frame",
                array("cmd"        => "edit",
                                              "GRADE"       => $row["GRADE"],
                                              "HR_CLASS"    => $row["HR_CLASS"],
                                              "term"        => $model->term)
            );
            $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";  //#ccffff
            //職員コードマスク処理
            for ($cdNm = 1; $cdNm <= 3; $cdNm++) {
                setTrcd("TR_CD{$cdNm}", $row, $model);
                setTrcd("SUBTR_CD{$cdNm}", $row, $model);
            }
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "list" && VARS::get("ed") != "1") {
            $arg["reload"] = "window.open('knja070index.php?cmd=edit&init=1','right_frame');";
        }

        View::toHTML($model, "knja070Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $getMaxSemester = $db->getOne(knja070Query::getMaxSemester());
    $nextYear = CTRL_YEAR + 1;
    $nextSemester = CTRL_SEMESTER + 1;
    if ($getMaxSemester == CTRL_SEMESTER) {
        $value = ($value && $value_flg) ? $value : $nextYear.'-1';
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.'-'.$nextSemester;
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//職員番号マスク用
function setTrcd($trName, &$row, $model)
{
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
