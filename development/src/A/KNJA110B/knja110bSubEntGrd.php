<?php

require_once('for_php7.php');

class knja110bSubEntGrd
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja110bindex.php", "", "sel");

        $db = Query::dbCheckOut();

        $query = knja110bQuery::getComeBackT();
        $isComeBack = $db->getOne($query) > 0 ? true : false;

        $rirekiFlg = false;
        $setKind = "";
        $query = knja110bQuery::getEntGrdHist($model, $isComeBack);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["RIREKI_FLG"] == "1") {
                $row["SCHOOL_KIND_NAME"] = "<a href=\"knja110bindex.php?cmd=entGrdEdit&SCHOOL_KIND={$row["SCHOOL_KIND"]}\">{$row["SCHOOL_KIND_NAME"]}</a>";
                $row["BGCOLOR"] = "#ffffff";
            } else {
                $row["SCHOOL_KIND_NAME"] = $row["SCHOOL_KIND_NAME"];
                $row["BGCOLOR"] = "#f0e68c";
            }
            if ($row["SCHOOL_KIND"] == "P" || $row["SCHOOL_KIND"] == "K") {
                $row["SET_SCHOOL_NAME"] = $row["NYUGAKUMAE_SYUSSIN_JOUHOU"];
            } else {
                $row["SET_SCHOOL_NAME"] = $row["FINSCHOOL_NAME"];
            } 
            $row["ENT_DATE"] = str_replace("-", "/", $row["ENT_DATE"]);
            $row["GRD_DATE"] = str_replace("-", "/", $row["GRD_DATE"]);
            $arg["rireki"][] = $row;
            $setKind = $row["SET_SKIND"] && $model->cmd != "changeEntGrdCmb" ? $row["SET_SKIND"] : $setKind;
            $rirekiFlg = true;
        }
        $result->free();

        $setRow = array();
        if (!isset($model->warning)) {
            if ($rirekiFlg && $model->grdEntHistField["SCHOOL_KIND"]) {
                $query = knja110bQuery::getEntGrdHistData($model, $model->schregno, $model->grdEntHistField["SCHOOL_KIND"]);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $setRow["SCHOOL_KIND"] = $model->grdEntHistField["SCHOOL_KIND"];
            } else if ($rirekiFlg && !$model->grdEntHistField["SCHOOL_KIND"]) {
                $model->grdEntHistField["SCHOOL_KIND"] = $setKind;
                $query = knja110bQuery::getEntGrdHistData($model, $model->schregno, $model->grdEntHistField["SCHOOL_KIND"]);
                $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $setRow =& $model->grdEntHistField;
            }
        } else {
            $setRow =& $model->grdEntHistField;
        }

        //学校種別
        $query = knja110bQuery::getSubSchoolKind();
        $extra = "onChange=\"return btn_submit('changeEntGrdCmb')\"";
        makeCmb($objForm, $arg, $db, $query, $setRow["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "BLANK");

        //出身中学校
        if ($setRow["SCHOOL_KIND"] == "P" || $setRow["SCHOOL_KIND"] == "K") {
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = knjCreateTextArea($objForm, "NYUGAKUMAE_SYUSSIN_JOUHOU", "4", "51", "wrap", $extra, $setRow["NYUGAKUMAE_SYUSSIN_JOUHOU"]);
        } else {
            $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
            $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);
            $extra = "";
            $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $setRow["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);
            $finschoolname = $db->getOne(knja110bQuery::getFinschoolName($setRow["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $setRow["FINSCHOOLNAME"] ? $setRow["FINSCHOOLNAME"] : $finschoolname;

            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE",str_replace("-","/",$setRow["FINISH_DATE"]),"");
        }

        //入学
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE",str_replace("-","/",$setRow["ENT_DATE"]),"");
        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $setRow["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);
        //入学区分
        $arg["data"]["ENT_DIV"] = $model->CreateCombo($objForm,$db,"A002","ENT_DIV",$setRow["ENT_DIV"],1);

        //事由
        $extra = "";
        $arg["data"]["ENT_REASON"] = knjCreateTextBox($objForm, $setRow["ENT_REASON"], "ENT_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["ENT_SCHOOL"] = knjCreateTextBox($objForm, $setRow["ENT_SCHOOL"], "ENT_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["ENT_ADDR"] = knjCreateTextBox($objForm, $setRow["ENT_ADDR"], "ENT_ADDR", 45, 90, $extra);

        //住所２使用(小学校、中学校)
        if ($model->Properties["useAddrField2"] == "1" && $model->grdEntHistField["SCHOOL_KIND"] != "H") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        //高校(住所2と同一フィールド名だが、項目名が異なる)
        } else if ($model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $arg["hyoujiFieldCourse"] = "1";
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $extra = "";
        $arg["data"]["ENT_ADDR2"] = knjCreateTextBox($objForm, $setRow["ENT_ADDR2"], "ENT_ADDR2", 45, 90, $extra);

        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE",str_replace("-","/",$setRow["GRD_DATE"]),"");
        $arg["data"]["GRD_DIV"] = $model->CreateCombo($objForm,$db,"A003","GRD_DIV",$setRow["GRD_DIV"],1);
        if ($setRow["SCHOOL_KIND"] != "H") {
            $arg["data"]["TENGAKU_SAKI_ZENJITU"] = View::popUpCalendar($objForm, "TENGAKU_SAKI_ZENJITU",str_replace("-","/",$setRow["TENGAKU_SAKI_ZENJITU"]),"");
        }
        $arg["data"]["TENGAKU_SAKI_GRADE"] = knjCreateTextBox($objForm, $setRow["TENGAKU_SAKI_GRADE"], "TENGAKU_SAKI_GRADE", 10, 15, $extra);

        //事由
        $extra = "";
        $arg["data"]["GRD_REASON"] = knjCreateTextBox($objForm, $setRow["GRD_REASON"], "GRD_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["GRD_SCHOOL"] = knjCreateTextBox($objForm, $setRow["GRD_SCHOOL"], "GRD_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["GRD_ADDR"] = knjCreateTextBox($objForm, $setRow["GRD_ADDR"], "GRD_ADDR", 45, 90, $extra);

        //学校住所2
        $extra = "";
        $arg["data"]["GRD_ADDR2"] = knjCreateTextBox($objForm, $setRow["GRD_ADDR2"], "GRD_ADDR2", 45, 90, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('entGrdHistAdd')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('entGrdHistUpd')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('entGrdHistDel')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //戻るボタン
        $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja110bSubEntGrd.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
