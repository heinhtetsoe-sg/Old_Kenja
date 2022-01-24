<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knje010aSubForm8 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform8", "POST", "knje010aindex.php", "", "subform8");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knje010aQuery::checkSchoolMst()) > 0) {
            $schoolcd       = sprintf("%012d", SCHOOLCD);
            $school_kind    = $db->getOne(knje010aQuery::getSchoolKind($model));
        }

        //学校マスタ情報
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $model->exp_year, $schoolcd, $school_kind);

        //出欠の記録
        $sick = $late = $early = 0;
        $result = $db->query(knje010aQuery::getAttendSemesDat($model, $knjSchoolMst));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SUSPEND"] != "") $suspend += (int)$row["SUSPEND"];
            if ($row["MOURNING"] != "") $mourning += (int)$row["MOURNING"];
            if ($row["SICK"] != "") $sick += (int)$row["SICK"];
            if ($row["LATE"] != "") $late += (int)$row["LATE"];
            if ($row["EARLY"] != "") $early += (int)$row["EARLY"];

            $arg["data"][] = $row;
        }
        $result->free();

        //停止チェックボックス
        $extra  = (strlen($suspend) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_SUSPEND\" onclick=\"return OptionUse(this);\"";
        $arg["CHECK_SUSPEND"] = knjCreateCheckBox($objForm, "CHECK_SUSPEND", $suspend, $extra, "");

        //忌引チェックボックス
        $extra  = (strlen($mourning) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_MOURNING\" onclick=\"return OptionUse(this);\"";
        $arg["CHECK_MOURNING"] = knjCreateCheckBox($objForm, "CHECK_MOURNING", $mourning, $extra, "");

        //欠席日数チェックボックス
        $extra  = (strlen($sick) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_SICK\" onclick=\"return OptionUse(this);\"";
        $arg["CHECK_SICK"] = knjCreateCheckBox($objForm, "CHECK_SICK", $sick, $extra, "");

        //遅刻チェックボックス
        $extra  = (strlen($late) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_LATE\" onclick=\"return OptionUse(this);\"";
        $arg["CHECK_LATE"] = knjCreateCheckBox($objForm, "CHECK_LATE", $late, $extra, "");

        //早退チェックボックス
        $extra  = (strlen($early) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_EARLY\" onclick=\"return OptionUse(this);\"";
        $arg["CHECK_EARLY"] = knjCreateCheckBox($objForm, "CHECK_EARLY", $early, $extra, "");

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('ATTENDREC_REMARK');\"";
        $arg["button"]["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010aSubForm8.html", $arg);
    }
}
?>