<?php

require_once('for_php7.php');

class knjz068bForm1
{
    public function main(&$model)
    {
        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz068bindex.php", "", "edit");

        //権限チェック
        //if (AUTHORITY != DEF_UPDATABLE) {
        //    $arg["jscript"] = "OnAuthError();";
        //}

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz068bQuery::getIBYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBYEAR", $model->ibyear, $extra, 1);

        //前年度からのコピーボタン
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からのコピー", $extra);

        //存在チェック（前年度）
        $pre_year = $model->ibyear - 1;
        $cnt_pre_year = $db->getOne(knjz068bQuery::checkIBSubclassUnitDat($pre_year));
        knjCreateHidden($objForm, "cnt_pre_year", $cnt_pre_year);

        //存在チェック（対象年度）
        $cnt_ibyear = $db->getOne(knjz068bQuery::checkIBSubclassUnitDat($model->ibyear));
        knjCreateHidden($objForm, "cnt_ibyear", $cnt_ibyear);

        //学年コンボ
        $query = knjz068bQuery::getIBGrade($model, "list");
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBGRADE", $model->ibgrade, $extra, 1);

        //IBコースコンボ
        $query = knjz068bQuery::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $model->ibprg_course, $extra, 1);

        if ($model->cmd == "combo") {
            unset($model->ibclasscd);
            unset($model->ibcurriculum_cd);
            unset($model->ibsubclasscd);
            $model->field = array();
            $model->field2 = array();
        }

        //表示名
        $arg["LABEL"] = ($model->ibprg_course == "M") ? 'Unit数' : 'Task数';

        //リスト作成
        makeList($arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjz068bindex.php?cmd=edit','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz068bForm1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $query = knjz068bQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //科目
        $row["IBSUBCLASS_SHOW"] = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].' <font size="2">'.$row["IBSUBCLASSNAME_ENG"].'</font>';

        //リンク番号
        $query = knjz068bQuery::getIBSubclassUnitDatLinkNo($model->ibyear, $model->ibgrade, $row["IBCLASSCD"], $row["IBPRG_COURSE"], $row["IBCURRICULUM_CD"], $row["IBSUBCLASSCD"]);
        $link_no = $db->getOne($query);

        //データ件数
        $ibseq_cnt = "";
        $acomma = "";
        $query = knjz068bQuery::getIBSubclassUnitDatCnt2($link_no);
        $resultCnt = $db->query($query);
        while ($rowCnt = $resultCnt->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ibseq_cnt .= $acomma . $rowCnt["IBSEQ_CNT"];
            $acomma = ",";
        }
        $resultCnt->free();
        $row["IBSEQ_CNT"] = $ibseq_cnt;

        $arg["data"][] = $row;
    }
    $result->free();
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
