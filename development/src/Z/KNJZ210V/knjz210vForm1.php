<?php

require_once('for_php7.php');

class knjz210vForm1
{
    var $dataRow = array(); //表示用一行分データをセット
    
    function main(&$model)
    {
         $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz210vindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR."年度";

        //学期コンボ作成
        $query = knjz210vQuery::getSemesterMst();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, $model);
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER_2", $model->semester_2, $extra, 1, $model);

        //テスト種別コンボ
        $extra = "onchange=\"return btn_submit('combo');\"";
        $query = knjz210vQuery::getTestcd($model, $model->semester);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->testcd, $extra, 1, $model);
        $extra = "";
        $query = knjz210vQuery::getTestcd($model, $model->semester_2);
        makeCmb($objForm, $arg, $db, $query, "TESTCD_2", $model->testcd_2, $extra, 1, $model);

        //学校区分名称取得
        $schooldiv = $db->getOne("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'");

        //コピー元／先のコピーボタン
        $arg["button"]["btn_copy_2"] = knjCreateBtn($objForm, "btn_copy_2", "上記を下記にコピーする。", "onclick=\"return btn_submit('copy_2');\"");

        //一覧表示
        $query = knjz210vQuery::SelectQuery($model);
        $cnt = get_count($db->getCol($query));
        if($cnt > 0){
            $result = $db->query($query);
            $this->DataRow = array();
            for ($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++) {
                if ($i == 0) {
                    $cd[] = $row["SUBCLASSCD"];
                }
                //科目コードが同じ間は表示しない
                if (in_array($row["SUBCLASSCD"], $cd)) {
                    $this->setDataRow($row, $model);
                } else {
                    $this->ModifyDataRow();
                    $arg["data"][] = $this->DataRow;

                    $cd = $this->DataRow = array();
                    $cd[] = $row["SUBCLASSCD"];
                    $this->setDataRow($row, $model);
                }
            }
            $this->ModifyDataRow();
            $result->free();
            $arg["data"][] = $this->DataRow;
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knjz210vindex.php?cmd=edit&SUBCLASSCD=XXXXXX','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz210vForm1.html", $arg); 
    }

    //表示用配列にデータ値をセット
    function setDataRow($row, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $this->DataRow["SUBCLASSCD"][] = View::alink("knjz210vindex.php", $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"],"target=right_frame",
                                      array("CLASSCD"       => $row["CLASSCD"],
                                            "SCHOOL_KIND"   => $row["SCHOOL_KIND"],
                                            "CURRICULUM_CD" => $row["CURRICULUM_CD"],
                                            "SUBCLASSCD"    => $row["SUBCLASSCD"],
                                            "cmd"           => "edit"));
        } else {
            $this->DataRow["SUBCLASSCD"][] = View::alink("knjz210vindex.php", $row["SUBCLASSCD"],"target=right_frame",
                                      array("SUBCLASSCD"   => $row["SUBCLASSCD"],
                                            "cmd"          => "edit"));
        }
        if ($row["SUBCLASSCD"] == $model->subclasscd) {
            unset($model->subclasscd);
            $row["CHAIRCD"] = ($row["CHAIRCD"]) ? $row["CHAIRCD"] : "　";
            $row["CHAIRCD"] = "<a name=\"target\">{$row["CHAIRCD"]}</a><script>location.href='#target';</script>";
        }

        //値が不揃いの場合は"*"を付ける
        $this->DataRow["SUBCLASSNAME"][] = $row["SUBCLASSNAME"];
        $this->DataRow["CHAIRCD"][] = $row["CHAIRCD"]." ".$row["CHAIRNAME"];

    }
    
    //表示用配列を表示できるように修正
    function ModifyDataRow()
    {
        if (isset($this->DataRow["SUBCLASSCD"])) $this->DataRow["SUBCLASSCD"] = implode("<BR>",array_unique($this->DataRow["SUBCLASSCD"]));
        if (isset($this->DataRow["SUBCLASSNAME"])) $this->DataRow["SUBCLASSNAME"] = implode("<BR>",array_unique($this->DataRow["SUBCLASSNAME"]));
        if (isset($this->DataRow["CHAIRCD"]))  $this->DataRow["CHAIRCD"] = implode("<BR>",array_unique($this->DataRow["CHAIRCD"]));
    }
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

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
