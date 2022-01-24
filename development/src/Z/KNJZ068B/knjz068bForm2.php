<?php

require_once('for_php7.php');

class knjz068bForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz068bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->cmd != "ibseq" && $model->cmd != "chgYear" && $model->ibyear && $model->ibgrade && $model->ibclasscd && $model->ibprg_course && $model->ibcurriculum_cd && $model->ibsubclasscd) {
            $query = knjz068bQuery::getIBSubclassUnitDat($model->ibyear, $model->ibgrade, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd, "", "row");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //IB科目コンボ
        $query = knjz068bQuery::getIBSubclasscd($model, "list");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"].'-'.$Row["IBCURRICULUM_CD"].'-'.$Row["IBSUBCLASSCD"];
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASS", $value, $extra, 1, "BLANK");

        //表示名
        $label = ($model->ibprg_course == "M") ? 'Unit' : 'Task';
        $arg["LABEL"] = $label;

        $disabled = ($Row["IBCLASSCD"] == "") ? "disabled " : "";

        //リンク番号
        $query = knjz068bQuery::getIBSubclassUnitDatLinkNo($model->ibyear, $model->ibgrade, $Row["IBCLASSCD"], $Row["IBPRG_COURSE"], $Row["IBCURRICULUM_CD"], $Row["IBSUBCLASSCD"]);
        $link_no = $db->getOne($query);

        //データ件数
        $query = knjz068bQuery::getIBSubclassUnitDatCnt($link_no);
        $ibseq_cnt = $db->getOne($query);
        $cnt = ($ibseq_cnt > 0) ? $ibseq_cnt : "";

        //Unit数
        $model->field["IBSEQ_CNT"] = (($model->cmd != "ibseq" && $model->cmd != "check" && $model->cmd != "chgYear") || $model->field["IBSEQ_CNT"] == "") ? $cnt : $model->field["IBSEQ_CNT"];
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["IBSEQ_CNT"] = knjCreateTextBox($objForm, $model->field["IBSEQ_CNT"], "IBSEQ_CNT", 3, 5, $disabled.$extra);

        //確定ボタン
        $extra = "onclick=\"return ibseq('{$cnt}', '{$label}');\"";
        $arg["button"]["btn_cnt"] = knjCreateBtn($objForm, "btn_cnt", "確 定", $disabled.$extra);

        //一覧表示
        for ($i = 1; $i <= $model->field["IBSEQ_CNT"]; $i++) {
            //データ取得
            $query = knjz068bQuery::getIBSubclassUnitDatList($link_no, $i);
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row2["IBSEQ"] = $i;
            $Row2["IBSEQ_SHOW"] = $label.$i;

            //年度コンボ
            $query = knjz068bQuery::getYear($model);
            if ($model->cmd == "check" || $model->cmd == "chgYear") {
                $Row2["YEAR"] = $model->field2["YEAR_".$i];
            }
            $extra = "onchange=\"return btn_submit('chgYear');\"";
            $Row2["YEAR_SHOW"] = makeCmb($objForm, $arg, $db, $query, "YEAR_".$i, $Row2["YEAR"], $extra, 1, "BLANK");

            //履修学期コンボ
            $query = knjz068bQuery::getSemesterMst($model, "list", $Row2["YEAR"]);
            if ($model->cmd == "check" || $model->cmd == "chgYear") {
                $Row2["SEMESTER"] = $model->field2["SEMESTER_".$i];
            }
            $Row2["SEMESTER"] = makeCmb($objForm, $arg, $db, $query, "SEMESTER_".$i, $Row2["SEMESTER"], "", 1, "BLANK");

            //学年コンボ
            $query = knjz068bQuery::getGrade($model, $Row2["YEAR"]);
            if ($model->cmd == "check" || $model->cmd == "chgYear") {
                $Row2["GRADE"] = $model->field2["GRADE_".$i];
            }
            $setGrade = $Row2["GRADE"];
            $extra = "onchange=\"return btn_submit('chgYear');\"";
            $Row2["GRADE"] = makeCmb($objForm, $arg, $db, $query, "GRADE_".$i, $Row2["GRADE"], $extra, 1, "BLANK");

            //科目コンボ
            $query = knjz068bQuery::getSubclasscd($model, "list", $Row2["YEAR"], $setGrade);
            $value = $Row2["CLASSCD"].'-'.$Row2["SCHOOL_KIND"].'-'.$Row2["CURRICULUM_CD"].'-'.$Row2["SUBCLASSCD"];
            if ($model->cmd == "check" || $model->cmd == "chgYear") {
                $value = ($model->field2["SUBCLASSCD_".$i]) ? $model->field2["CLASSCD_".$i].'-'.$model->field2["SCHOOL_KIND_".$i].'-'.$model->field2["CURRICULUM_CD_".$i].'-'.$model->field2["SUBCLASSCD_".$i] : "";
            }
            $Row2["SUBCLASS"] = makeCmb($objForm, $arg, $db, $query, "SUBCLASS_".$i, $value, "", 1, "BLANK");

            $arg["data"][] = $Row2;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GETIB_YEAR", $model->ibyear);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2" && $model->cmd != "ibseq") {
            $arg["reload"] = "window.open('knjz068bindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz068bForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK = "")
{
    $opt = array();
    $value_flg = false;
    if ($BLANK) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    if ($name == "IBSUBCLASS") {
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $combo = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
        return $combo;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ボタンの有効
    $disabled = ($model->field["IBSEQ_CNT"] > 0) ? "" : "disabled";

    //登録ボタン
    $extra = " onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $disabled.$extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $disabled.$extra);
    //取消ボタン
    $extra = " onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = " onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    //ＣＳＶボタン
    $extra = " onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}
