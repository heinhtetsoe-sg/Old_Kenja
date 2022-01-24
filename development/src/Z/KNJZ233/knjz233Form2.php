<?php

require_once('for_php7.php');

class knjz233Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz233index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        $ChosenData = makeTitle($arg, $db, $model);

        //教科名称取得
        $className = $db->getOne(knjz233Query::GetClassName($model));
        $arg["rightclasscd"] = $className;

        //科目リストToリスト作成
        $leftCnt = makeSubclassList($objForm, $arg, $db, $model);

        //単位固定／加算ラジオボタン 1:固定 2:加算
        $opt = array(1, 2);
        $model->calculate_credit_flg = (!$model->calculate_credit_flg) ? 1 : $model->calculate_credit_flg;
        $extra = array("id=\"CALCULATE_CREDIT_FLG1\"", "id=\"CALCULATE_CREDIT_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "CALCULATE_CREDIT_FLG", $model->calculate_credit_flg, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if ($model->record_dat_flg == "1"){
            $arg["show_confirm"] = "Show_Confirm();";
        }

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz233index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz233Form2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db, $model)
{
    $ChosenData = array();
    $ChosenData = $db->getRow(knjz233Query::getChosenData($model, $model->subclasscd),DB_FETCHMODE_ASSOC);
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $arg["info"]    = array("TOP"        => "合併先科目 : ".$ChosenData["CLASSCD"]."-".$ChosenData["SCHOOL_KIND"]."-".$ChosenData["CURRICULUM_CD"]."-".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "合併元科目",
                                "RIGHT_LIST" => "科目一覧(専門)" );
    } else {
        $arg["info"]    = array("TOP"        => "合併先科目 : ".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "合併元科目",
                                "RIGHT_LIST" => "科目一覧" );
    }
    return $ChosenData;
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, &$model)
{
    $leftCnt = 0;   //合併登録件数
    $opt_left = $opt_right = array();
    if (isset($model->subclasscd)) {

        $result      = $db->query(knjz233Query::selectQuery($model, $model->subclasscd, $model->rightclasscd, $model->school_kind, $model->curriculum_Cd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ATTEND_SUBCLASSCD"]) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_left[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_left[]  = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["SUBCLASSCD"]);
                }
                $model->calculate_credit_flg = $row["CALCULATE_CREDIT_FLG"];
                $leftCnt++;
            } else {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_right[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_right[]  = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["SUBCLASSCD"]);
                }
            }
        }
        $result->free();
    }

    //合併元科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = createCombo($objForm, "classyear", "right", $opt_left, $extra, 20);
    //科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = createCombo($objForm, "classmaster", "left", $opt_right, $extra, 20);
    //各種ボタン
    $arg["main_part"]["SEL_ADD_ALL"] = createBtn($objForm, "sel_add_all", "≪", "onclick=\"return moves('sel_add_all');\"");
    $arg["main_part"]["SEL_ADD"]     = createBtn($objForm, "sel_add", "＜", "onclick=\"return move('left');\"");
    $arg["main_part"]["SEL_DEL"]     = createBtn($objForm, "sel_del", "＞", "onclick=\"return move('right');\"");
    $arg["main_part"]["SEL_DEL_ALL"] = createBtn($objForm, "sel_del_all", "≫", "onclick=\"return moves('sel_del_all');\"");

    return $leftCnt;
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタン
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更新", "onclick=\"return doSubmit();\"");
    //取消ボタン
    $arg["button"]["btn_clear"] = createBtn($objForm, "btn_clear", "取消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
    $objForm->ae(createHiddenAe("record_dat_flg", 0));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
