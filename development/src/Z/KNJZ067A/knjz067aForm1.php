<?php

require_once('for_php7.php');

class knjz067aForm1
{
    public function main(&$model)
    {
        $arg["reload"] = "";

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz067aindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz067aQuery::getIBYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBYEAR", $model->ibyear, $extra, 1, $model);

        //学年コンボ
        $query = knjz067aQuery::getIBGrade($model, "list");
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "IBGRADE", $model->ibgrade, $extra, 1, $model);

        if ($model->cmd == "combo") {
            unset($model->ibclasscd);
            unset($model->ibprg_course);
            unset($model->ibcurriculum_cd);
            unset($model->ibsubclasscd);
        }

        //前年度からのコピーボタン
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からのコピー", $extra);

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
            $arg["reload"] = "window.open('knjz067aindex.php?cmd=edit','right_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz067aForm1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $bifKey1 = $bifKey2 = "";
    $query = knjz067aQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //IB科目
        $row["IBSUBCLASS_SHOW"] = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"].' '.$row["IBSUBCLASSABBV_ENG"];
        //科目
        $row["SUBCLASS_SHOW"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].' '.$row["SUBCLASSNAME"];

        //列結合
        if ($bifKey1 !== $row["IBPRG_COURSE"]) {
            $cnt1 = $db->getOne(knjz067aQuery::getRowDataCnt($row, "1"));
            $row["ROWSPAN1"] = $cnt1 > 0 ? $cnt1 : 1;
        }
        if ($bifKey2 !== $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"]) {
            $cnt2 = $db->getOne(knjz067aQuery::getRowDataCnt($row, "2"));
            $row["ROWSPAN2"] = $cnt2 > 0 ? $cnt2 : 1;
        }
        $bifKey1 = $row["IBPRG_COURSE"];
        $bifKey2 = $row["IBCLASSCD"].'-'.$row["IBPRG_COURSE"].'-'.$row["IBCURRICULUM_CD"].'-'.$row["IBSUBCLASSCD"];

        $arg["data"][] = $row;
    }
    $result->free();
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model)
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
