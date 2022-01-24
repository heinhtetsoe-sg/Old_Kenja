<?php

require_once('for_php7.php');
class knje065bForm1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje065bindex.php", "", "edit");

        //コントロールマスタの学籍処理年度で学年進行データを検索。
        $db     = Query::dbCheckOut();
        $result = $db->query(knje065bQuery::getGradeQuery($model));

        //処理年度と処理学期
        $arg["TOP"] = array("TRANSACTION"   => CTRL_YEAR,
                            "SEMESTER"      => $model->control_data["学期名"][CTRL_SEMESTER]);

        //処理学年
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade = ltrim($row["GRADE"], "0");
            $label = $row["GRADE_NAME1"];
            if ($label == '') {
                $label = $grade."学年";
            }
            $opt[] = array("label" => $label,
                           "value" => $row["GRADE"]);
        }
        if ($model->gc_select == "") {
            $model->gc_select = $opt[0]["value"];
        }
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["gc_select"] = knjCreateCombo($objForm, "gc_select", $model->gc_select, $opt, $extra, 1);
        $result->free();

        //卒業履修単位数取得
        $model->gradCompCredits = $db->getOne(knje065bQuery::getSchoolMst($model, "GRAD_COMP_CREDITS"));

        //学校区分取得（0:学年制 1:単位制）
        $model->schoolDiv = $db->getOne(knje065bQuery::getSchoolMst($model, "SCHOOLDIV"));

        //RECORD_PROV_FLG_DAT存在チェック
        $model->isRecordProvFlgDat = ($db->getOne(knje065bQuery::checkTable("RECORD_PROV_FLG_DAT")) > 0) ? true: false;

        //異動基準日
        $arg["base_date"] = View::popUpCalendar($objForm, "base_date", $model->base_date);

        //選択科目を除く
        $extra = ($model->electdiv == "on") ? "checked" : "";
        $arg["electdiv"] = knjCreateCheckBox($objForm, "electdiv", "on", $extra, "");

        //評定読替するかしないかのフラグ 1:表示 1以外:非表示
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["hyoteiYomikaeFlg"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["hyoteiYomikaeFlg"]);
        }

        //評定読替チェックボックス
        $extra  = ($model->hyoteiYomikae == "1") ? "checked" : "";
        $extra .= " id=\"hyoteiYomikae\"";
        $arg["hyoteiYomikae"] = knjCreateCheckBox($objForm, "hyoteiYomikae", "1", $extra, "");

        //インラインフレーム
        if ($model->Properties["gaihyouGakkaBetu"] !== '1' && $model->Properties["gaihyouGakkaBetu"] !== '2') {
            unset($arg["GAKKA_BETU_FLG"]);
            $arg["SET_NAME"] = '課程学科';
            $arg["GAKKA_BETU_HEAD"] = '学科別';
            $arg["GAKKA_BETU_TITLE"] = '学科人数';
        } elseif ($model->Properties["gaihyouGakkaBetu"] === '1') {
            $arg["GAKKA_BETU_FLG"] = 1; //null以外なら何でもいい
            $arg["SET_NAME"] = '課程学科';
            $arg["GAKKA_BETU_HEAD"] = 'コース別';
            $arg["GAKKA_BETU_TITLE"] = 'コース人数';
        } elseif ($model->Properties["gaihyouGakkaBetu"] === '2') {
            $arg["SET_NAME"] = 'コースグループ名称';
            $arg["GAKKA_BETU_HEAD"] = 'コースグループ別';
            $arg["GAKKA_BETU_TITLE"] = '<font size= "1">'.'コース'.'<BR>'.'グループ人数'.'</font>';
        }

        if ($model->Properties["knje065b_showTajuHeikin"] == "1") {
            $arg["showTajuHeikin"] = "1";
            // "" : 単純平均
            // "01" : 加重平均
            $opt = array();
            $opt[] = array("value" => ""  , "label" => "単純平均");
            $opt[] = array("value" => "01", "label" => "加重平均");
            $extra = "onChange=\"return btn_submit('main');\"";
            $arg["KINDCD"] = knjCreateCombo($objForm, "KINDCD", $model->fields["KINDCD"], $opt, $extra, 1);
        }

        //インラインフレーム
        $query = knje065bQuery::getZ010();
        $model->z010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->z010["GAKUNENSEI"] = $model->z010["NAMESPARE2"] == "1" || $model->z010["NAMESPARE2"] == "2" ? "1" : "0";
        $model->useRecordScoreDat = $model->z010["NAMESPARE1"] != "" ? true: fasle;
        if ($model->cmd == "recalc") {
            $query = knje065bQuery::getRecalculateQuery($model);
        } else {
            $query = knje065bQuery::readQuery($model);
        }
        $result = $db->query($query);
        unset($model->fields["CODE"]);

        $count=0;
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->Properties["gaihyouGakkaBetu"] === '2') {
                $model->fields["CODE"][] = $Row["COURSECD"].",".$Row["GROUP_CD"].",".$Row["COURSECODE"];
            } else {
                $model->fields["CODE"][] = $Row["COURSECD"].",".$Row["MAJORCD"].",".$Row["COURSECODE"];
            }
            //コース人数合計
            $Row["COURSE_MEMBER"] = $Row["A_MEMBER"]+$Row["B_MEMBER"]+$Row["C_MEMBER"]+$Row["D_MEMBER"]+$Row["E_MEMBER"];

            //text
            $a = ($Row["A_MEMBER"] == "")? "0" : $Row["A_MEMBER"] ;
            $extra = "style=\"text-align:right\" onblur=\"sumNum(this)\" ";
            $Row["A_MEMBER"] = knjCreateTextBox($objForm, $a, "A_MEMBER", 4, 3, $extra, "1");

            $b = ($Row["B_MEMBER"] == "")? "0" : $Row["B_MEMBER"] ;
            $extra = "style=\"text-align:right\" onblur=\"sumNum(this)\" ";
            $Row["B_MEMBER"] = knjCreateTextBox($objForm, $b, "B_MEMBER", 4, 3, $extra, "1");

            $c = ($Row["C_MEMBER"] == "")? "0" : $Row["C_MEMBER"] ;
            $extra = "style=\"text-align:right\" onblur=\"sumNum(this)\" ";
            $Row["C_MEMBER"] = knjCreateTextBox($objForm, $c, "C_MEMBER", 4, 3, $extra, "1");

            $d = ($Row["D_MEMBER"] == "")? "0" : $Row["D_MEMBER"] ;
            $extra = "style=\"text-align:right\" onblur=\"sumNum(this)\" ";
            $Row["D_MEMBER"] = knjCreateTextBox($objForm, $d, "D_MEMBER", 4, 3, $extra, "1");

            $e = ($Row["E_MEMBER"] == "")? "0" : $Row["E_MEMBER"] ;
            $extra = "style=\"text-align:right\" onblur=\"sumNum(this)\" ";
            $Row["E_MEMBER"] = knjCreateTextBox($objForm, $e, "E_MEMBER", 4, 3, $extra, "1");

            if ($model->cmd == "recalc") {
                //学年人数合計
                $total = $db->getOne(knje065bQuery::getAllCntQuery($model, $Row));
                $extra = "style=\"text-align:right\" onblur=\"getZero(this)\" ";
                $Row["GRADE_MEMBER"] = knjCreateTextBox($objForm, $total, "GRADE_MEMBER", 4, 3, $extra, "1");
            } else {
                $gm = ($Row["GRADE_MEMBER"] == "")? "0" : $Row["GRADE_MEMBER"] ;
                $extra = "style=\"text-align:right\" onblur=\"getZero(this)\" ";
                $Row["GRADE_MEMBER"] = knjCreateTextBox($objForm, $gm, "GRADE_MEMBER", 4, 3, $extra, "1");
            }
            $Row["INDEX"] = $count;
            $arg["data"][] = $Row;
            $count++;
        }
        //ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);

        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);

        $extra = "onclick=\"return closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);

        if ($model->Properties["KNJE065B_SetCommentExACD"] == "1") {
            $arg["MARU_APARAM"] = "1";
        }

        //学籍在籍データ、評定マスタが作成済みかチェック
        $getSchSchregno = $db->getOne(knje065bQuery::getSchSchregno());
        $getAssesscd    = $db->getOne(knje065bQuery::getAssesscd($model));

        knjCreateHidden($objForm, "SCH_CNT", $getSchSchregno);
        knjCreateHidden($objForm, "ASS_CNT", $getAssesscd);

        knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje065bForm1.html", $arg);
    }
}
