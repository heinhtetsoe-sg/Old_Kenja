<?php

require_once('for_php7.php');

/********************************************************************/
/* 振込依頼書                                       山城 2005/11/27 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：KNJP370_2返戻通知書追加                  山城 2006/03/23 */
/* ･NO002：振込依頼書出力時に日付をテーブルに保存   山城 2006/03/28 */
/* ･NO003：振込依頼書に生活行事費を追加する。       山城 2006/03/29 */
/* ･NO004：軽減をKNJP371にする                      山城 2006/05/07 */
/* ･NO005：小分類を選択可能にする。                 山城 2006/05/07 */
/********************************************************************/

class knjp370Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp370Form1", "POST", "knjp370index.php", "", "knjp370Form1");

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //担当者コンボを作成する
        $this->makeStaff($objForm, $arg, $model);

        //取扱指定日
        if ($model->date == "") $model->date = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->date);

        //帳票種類ラジオを作成
        $this->makeOutput($objForm, $arg, $model);

        //通知書テキストを作成
        $this->makeRepayText($objForm, $arg, $model);

        //小分類リストの設定
        $inSentence = $this->makeExpensList($objForm, $arg, $model);

        //クラス選択コンボボックスの設定
        $this->makeClassCmb($objForm, $arg, $model, $inSentence);

        //対象者リストの設定
        $this->makeStudentCmb($objForm, $arg, $model, $inSentence);

        //印刷ボタン
        if ($model->output == "1"){
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        } else {
            $extra = "onclick=\"return btn_submit('update');\"";
        }
        $arg["button"]["btn_print"] = $this->createBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成
        $this->makeHidden($objForm, $arg);

        if (!isset($model->warning) && $model->cmd == 'read'){
            $model->cmd = 'knjp370';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp370Form1.html", $arg); 
    }

    //担当者作成
    function makeStaff(&$objForm, &$arg, &$model)
    {
        $opt_staff = array();
        $opt_staff = $this->setStaff($model);
        $arg["data"]["STAFF"] = $this->createCombo($objForm, "STAFF", $model->staffcd, $opt_staff, "", 1);
    }

    //担当者をセット
    function setStaff(&$model)
    {
        $opt_staff = array();

        $db = Query::dbCheckOut();
        $result = $db->query(knjp370Query::GetStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_staff[] = array("label" => $row["STAFFNAME"],
                                 "value" => $row["STAFFCD"]);
        }

        if (!$model->staffcd) $model->staffcd = $opt_staff[0]["value"];

        $result->free();
        Query::dbCheckIn($db);
        return $opt_staff;
    }

    //帳票種類作成
    function makeOutput(&$objForm, &$arg, &$model)
    {
        $opt_out = array();
        $opt_out[0] = 1;
        $opt_out[1] = 2;

        if (!$model->output){
            $model->output = 1;
        }

        $objForm->ae($this->createRadio("OUTPUT", $model->output, $opt_out));
        for ($i = 0; $i < get_count($opt_out); $i++) {
            $arg["data"]["OUTPUT".$opt_out[$i]] = $objForm->ge("OUTPUT",$opt_out[$i]);
        }
    }

    //返戻事由作成
    function makeRepayText(&$objForm, &$arg, &$model)
    {
        $this->setText($model);
        $arg["data"]["TEXT1"] = $this->createText($objForm, "TEXT1", $model->text1, "", 30, 15);
        $arg["data"]["TEXT2"] = $this->createText($objForm, "TEXT2", $model->text2, "", 30, 15);
    }

    //テキストデータをセット
    function setText(&$model)
    {
        $db = Query::dbCheckOut();
        $query = knjp370Query::getText();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($row["DIV"] == "0001" && !$model->text1 && !$model->cmd == "change_class"){
                $model->text1 = $row["REMARK"];
            }else if ($row["DIV"] == "0002" && !$model->text2 && !$model->cmd == "change_class"){
                $model->text2 = $row["REMARK"];
            }
        }

        $result->free();
        Query::dbCheckIn($db);
    }

    //小分類リスト作成
    function makeExpensList(&$objForm, &$arg, &$model)
    {
        $db = Query::dbCheckOut();
        $opt_due  = array();
        $due_left = array();
        $selectleft2 = explode(",", $model->selectleft2);
        $query = knjp370Query::GetPaids($model);
        $result = $db->query($query);

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt2[$row["EXPENSE_M_CD"].":".$row["EXPENSE_S_CD"].":".$row["REPAY_MONEY_DATE"].":"] = array ("label" => $row["REPAY_MONEY_DATE"]."　".$row["EXPENSE_S_NAME"], 
                                                                                                                         "value" => $row["EXPENSE_M_CD"].":".$row["EXPENSE_S_CD"].":".$row["REPAY_MONEY_DATE"].":");

            if($model->cmd == 'change_class' || $model->cmd == 'read') {
                if (!in_array($row["EXPENSE_M_CD"].":".$row["EXPENSE_S_CD"].":".$row["REPAY_MONEY_DATE"].":", $selectleft2)){
                    $opt_due[] = array ('label' => $row["REPAY_MONEY_DATE"]."　".$row["EXPENSE_S_NAME"],
                                        'value' => $row["EXPENSE_M_CD"].":".$row["EXPENSE_S_CD"].":".$row["REPAY_MONEY_DATE"].":");
                }
            }else {
                $opt_due[] = array ('label' => $row["REPAY_MONEY_DATE"]."　".$row["EXPENSE_S_NAME"],
                                    'value' => $row["EXPENSE_M_CD"].":".$row["EXPENSE_S_CD"].":".$row["REPAY_MONEY_DATE"].":");
            }
        }
        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' || $model->cmd == 'read') {
            foreach ($model->select_opt2 as $key => $val){
                if (in_array($key, $selectleft2)) {
                    $due_left[] = $val;
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $disable = 1;

        //小分類リストの設定
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"duemove('left',$disable)\"";
        $arg["data"]["DUE_NAME"] = $this->createCombo($objForm, "due_name", "", $opt_due, $extra, 5);

        //対象小分類リストの設定
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"duemove('right',$disable)\"";
        $arg["data"]["DUE_SELECTED"] = $this->createCombo($objForm, "due_selected", "", $due_left, $extra, 5);

        /****************/
        /* ボタンの設定 */
        /****************/

        //対象選択ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"duemoves('right',$disable);\"";
        $arg["button"]["due_rights"] = $this->createBtn($objForm, "due_rights", ">>", $extra);

        //対象取消ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"duemoves('left',$disable);\"";
        $arg["button"]["due_lefts"] = $this->createBtn($objForm, "due_lefts", "<<", $extra);

        //対象選択ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"duemove('right',$disable);\"";
        $arg["button"]["due_right1"] = $this->createBtn($objForm, "due_right1", "＞", $extra);

        //対象取消ボタンの設定
        $extra = "style=\"height:20px;width:40px\" onclick=\"duemove('left',$disable);\"";
        $arg["button"]["due_left1"] = $this->createBtn($objForm, "due_left1", "＜", $extra);

        //SQL用データ作成
        $inSentence = "(";
        $inSep = "";
        if (get_count($due_left) == 0){
            $inSentence .= "''";
        }else {
            foreach ($due_left as $key => $val){
                $inSentence .= $inSep."'".str_replace(":","",$val["value"])."'";
                $inSep = ",";
            }
        }
        $inSentence .= ")";

        return $inSentence;
    }

    //クラスコンボ作成
    function makeClassCmb(&$objForm, &$arg, &$model, $inSentence)
    {
        $opt_class = array();
        $db = Query::dbCheckOut();
        $query = knjp370Query::getclass($model,$inSentence);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_class[]= array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $opt_class[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('change_class');\"";
        $arg["data"]["GRADE_HR_CLASS"] = $this->createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt_class, $extra, 1);

    }

    //生徒一覧作成
    function makeStudentCmb(&$objForm, &$arg, &$model, $inSentence)
    {
        $db = Query::dbCheckOut();
        $opt1 = array();
        $opt_left = array();

        //生徒単位
        $selectleft = explode(",", $model->selectleft);
        $db = Query::dbCheckOut();
        $select_data = array();

        $query = knjp370Query::getrepai2($model,$inSentence);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $select_data[$row["SCHREGNO"]] = $row["SCHREGNO"];
        }


        $query = knjp370Query::getrepai($model,$inSentence);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");

            if($model->cmd == 'change_class' || $model->cmd == 'read') {
                if (!in_array($row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":", $selectleft)){
                    $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
                }
            }else {
                $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
            }
        }
        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' || $model->cmd == 'read') {
            foreach ($model->select_opt as $key => $val){
                if (in_array($key, $selectleft)) {
                    if ($select_data[$key]) {
                        $opt_left[] = $val;
                    }
                }
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        $disable = 1;

        //生徒一覧リストの設定
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"";
        $arg["data"]["CATEGORY_NAME"] = $this->createCombo($objForm, "category_name", "", $opt1, $extra, 15);

        //対象生徒リストの設定
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
        $arg["data"]["CATEGORY_SELECTED"] = $this->createCombo($objForm, "category_selected", "", $opt_left, $extra, 15);

        /****************/
        /* ボタンの設定 */
        /****************/

        //対象選択ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"";
        $arg["button"]["btn_rights"] = $this->createBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"";
        $arg["button"]["btn_lefts"] = $this->createBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"";
        $arg["button"]["btn_right1"] = $this->createBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"";
        $arg["button"]["btn_left1"] = $this->createBtn($objForm, "btn_left1", "＜", $extra);
    }

    //Hidden設定
    function makeHidden(&$objForm, &$arg)
    {
        $arg["TOP"]["YEAR"]     = $this->createHiddenGe($objForm, "YEAR", CTRL_YEAR);
        $arg["TOP"]["SEMESTER"] = $this->createHiddenGe($objForm, "SEMESTER", CTRL_SEMESTER);
        $arg["TOP"]["DBNAME"]   = $this->createHiddenGe($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"]    = $this->createHiddenGe($objForm, "PRGID", "KNJP370");

        $objForm->ae($this->createHiddenAe("cmd"));
        $objForm->ae($this->createHiddenAe("selectleft"));
        $objForm->ae($this->createHiddenAe("selectleft2"));
    }

    //コンボ/リスト作成
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

    //テキスト作成
    function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
    {
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "extrahtml" => $extra,
                            "value"     => $value));
        return $objForm->ge($name);
    }

    //ラジオ作成
    function createRadio($name, $value, $options) {
        $opt_radio = array();
        $opt_radio = array("type"      => "radio",
                           "name"      => $name,
                           "value"     => $value,
                           "options"   => $options);
        return $opt_radio;
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

    //Hidden作成ge
    function createHiddenGe(&$objForm, $name, $value = "")
    {
        $objForm->ae( array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value));
        return $objForm->ge($name);
    }

}
?>
