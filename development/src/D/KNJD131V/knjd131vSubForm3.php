<?php

require_once('for_php7.php');

class knjd131vSubForm3
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knjd131vindex.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        $ttlName = $db->getOne(knjd131vQuery::getNameMst());
        //タイトル
        if ($model->cmd == "subform3") {
            $arg["TITLE"] = $ttlName;
            $teikeiCmd = "subform3";
        }

        //学年
        $query = knjd131vQuery::getGrade($model);
        $grade = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["GRADE"] = $grade["GRADE_NAME1"];
        knjCreateHidden($objForm, "GRADE", $model->grade);

        //リスト作成
        $datacnt = makeList($objForm, $arg, $db, $model);
        //ボタン作成
        makeBtn($objForm, $arg, $datacnt);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd131vSubForm3.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $datacnt = 0;
    $query = knjd131vQuery::getHtrainRemarkTempDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["REMARK"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        $arg["data"][] = $row;
        $datacnt++;
    }
    $result->free();
    return $datacnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $datacnt)
{
    //選択ボタン
    $extra = "onclick=\"return btn_submit2('".$datacnt."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
