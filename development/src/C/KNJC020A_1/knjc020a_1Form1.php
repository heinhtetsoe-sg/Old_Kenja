<?php

require_once('for_php7.php');

class knjc020a_1Form1
{
    function main(&$model)
    {
        $start = $model->getMicrotime();

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc020a_1index.php", "", "edit");

        //dbopen
        $db = Query::dbCheckOut();

        $thisMonth = array();
        $thisMonth = explode("-",$model->executedate);

        //学期取得
        $query = knjc020a_1Query::getTerm($model->executedate);
        $model->semester = $db->getOne($query);

        //講座情報取得
        $query = knjc020a_1Query::getTargetChair($model);
        $result = $db->query($query);
        //コンボボックス用データ
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
           $opt[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                          "value" => $row["CHAIRCD"]);
        }

        if($model->chaircd == "" || $model->chaircd == "undefined" || is_null($model->chaircd)){
            $model->chaircd = $model->t_carcd;
            if($model->t_carcd == "" || $model->t_carcd == "undefined" || is_null($model->t_carcd)){
                $model->chaircd = $opt[0]["value"];
            }
        }

        //メインデータ取得
        $query = knjc020a_1Query::getTimeTable($model);
        $result = $db->query($query);

        $setValueArray = array();
        $model->hrClasses = array();
        $sep = "";

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($model->chaircd == $row["CHAIRCD"]) {
                //同じスタッフを消去
                $arg["STAFF"][$row["STAFFCD"]] = $row["STAFFNAME_SHOW"];
                //同じ施設を消去
                $arg["FAC"][$row["FACCD"]] = $row["FACILITYNAME"];
                //同じ科目名を削除
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $arg["SUBCLASS"][$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
                } else {
                    $arg["SUBCLASS"][$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
                }
                //同じクラスを消去
                $trgtGradeHr = $row["TRGTGRADE"].$row["TRGTCLASS"];
                $arg["TRGTCLASS"][$trgtGradeHr]["NAME"] = $row["LESSON_NAME"].$row["TARGETCLASS"];
                $arg["TRGTCLASS"][$trgtGradeHr]["COUNTFLG"] = $row["COUNTFLG"];
                $model->hrClasses[$trgtGradeHr] = $row["TRGTGRADE"]."-".$row["TRGTCLASS"];
                //講座名
                $arg["CHAIRNAME"] = " ".$row["CHAIRCD"]."  ".$row["CHAIRNAME"];
                //出欠済み/出欠未
                $arg["EXECUTED"] = ($row["EXECUTED"] == "1" )? "済み" : "未" ;
                //確認者
                $arg["ATTESTOR"] = " ".$row["ATTESTOR"]."  ".$row["ATTESTOR_NAME"];
            }
            //講座コンボのラベル用
            for ($i = 0; $i < get_count($opt); $i++) {
                if ($opt[$i]["value"] == $row["CHAIRCD"]) {
                    $setValueArray[$row["CHAIRCD"]] = $row["EXECUTED"] == "1" ? "済　" : "未　";
                }
            }
        }
        //講座コンボのラベルに済/未を加える
        for ($i = 0; $i < get_count($opt); $i++) {
            $opt[$i]["label"] = $setValueArray[$opt[$i]["value"]].$opt[$i]["label"];
        }

        //スタッフを整列
        foreach( $arg["STAFF"] as $key => $val){
            if(isset($arg["STAFFNAME"])){
                $arg["STAFFNAME"] .= ", ".$key." ".$val;
            }else{
                $arg["STAFFNAME"] = $key." ".$val;
            }
        }
        //施設を整列
        foreach( $arg["FAC"] as $key => $val){
            if(isset($arg["FACNAME"])){
                $arg["FACNAME"] .= ", ".$key." ".$val;
            }else{
                $arg["FACNAME"] = $key." ".$val;
            }
        }
        //施設を整列
        foreach( $arg["SUBCLASS"] as $key => $val){
            if(isset($arg["SUBCLASSNAME"])){
                $arg["SUBCLASSNAME"] .= ", ".$val;
            }else{
                $arg["SUBCLASSNAME"] = $val;
            }
        }

        //テスト時間割チェック
        $sch_test = $db->getOne(knjc020a_1Query::checkSchTest($model));

        //テスト時間割の集計フラグ
        $test_countflg = $db->getOne(knjc020a_1Query::getTestCountflg($model));

        //CHECK_ALL
        $extra  = " id=\"CHECKALL\" ";
        $extra .= "onClick=\"return check_all(this);\"";
        $extra .= ($sch_test == 0) ? "" : "disabled";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");
        //クラスを整列
        $classCnt = 1;
        $sepBr = "";
        foreach ($arg["TRGTCLASS"] as $key => $val) {
            if ($classCnt > 10) {
                $sepBr = "<BR>";
                $classCnt = 1;
            }
            if($sch_test == 0) {
                $extra  = $val["COUNTFLG"] ? " checked" : "";
            } else {
                $extra  = $test_countflg ? " checked" : "";
                $extra .= " disabled";
            }
            $arg["TRGTCLASSNAME"] .= knjCreateCheckBox($objForm, "CLASS_CHECK", $key, $extra, 1).$val["NAME"]." ".$sepBr;
            $sepBr = "";
            $classCnt++;
        }
        //科目を整列
        foreach( $arg["SUBCLASS"] as $key => $val){
            if(isset($arg["SUBCLASS_SHOW"])){
                $arg["SUBCLASS_SHOW"] .= ", ".$key." ".$val;
            }else{
                $arg["SUBCLASS_SHOW"] = $key." ".$val;
            }
        }

        if($model->mode == "on")
        {
            $confirmScript  = ($setValueArray[$model->chaircd] == "済　") ? "confirm" : "allupdate" ;
        }
        $model->executed_flg = ($setValueArray[$model->chaircd] == "済　")? true : false ;

        //DBクローズ
        $result->free();
        Query::dbCheckIn($db);

        if(get_count($opt) > 1){
            //科目コンボボックスを作成する
            $objForm->ae( array("type"        => "select",
                                "name"        => "chaircombo",
                                "size"        => "1",
                                "value"       => $model->chaircd,
                                "options"     => $opt,
                                "extrahtml"   => "onChange=\"btn_submit('');\";" ) );

            $arg["chaircombo"] = $objForm->ge("chaircombo");
        }else{
            $arg["chaircombo"] = $arg["CHAIRNAME"];
        }

        if($model->mode == "on")
        {
            //キャプション
            $cap = ($confirmScript == "confirm")? "入力取消" : "全員出席";
            //確認済みボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_done",
                                "value"     => " 確認済 ",
                                "extrahtml" => " onclick=\"return btn_submit('execute');\"" ) );
            //入力取消/全員出席ボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_cancel",
                                "value"     => $cap,
                                "extrahtml" => " onclick=\"return btn_submit('".$confirmScript."');\"" ) );
            //出欠入力画面へボタン
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_jump",
                                "value"     => " 出欠入力画面へ ",
                                "extrahtml" => " onclick=\"return btn_submit('jmp');\"" ) );

            $arg["btn"] = array("done"   =>  $objForm->ge("btn_done"),
                                "cancel" =>  $objForm->ge("btn_cancel"),
                                "jump"   =>  $objForm->ge("btn_jump") );
        }

        //更新ボタンを作成する
        if($sch_test == 0) {
            $extra = " onclick=\"return btn_submit('update');\"";
            $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", " 更 新 ", $extra);
        }

        //戻るボタンを作成する
        $extra = " onclick=\"return btn_submit('retParent');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", " 戻 る ", $extra);

        //hidden作成
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd") );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "executed_flg",
                            "value" => $model->executed_flg ) );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "update_type") );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "chg",
                            "value" => $model->chenge_flg));

        if($model->cmd == "jmp"){
            //リンク先作成
            $link  = "/C/KNJC010A";
            $link  = REQUESTROOT.$link."/knjc010aindex.php";
            Query::dbCheckIn($db);

            $arg["jscp"] = " IsUserOK_ToJump('".$link."','".$model->executedate."','".$model->priod."','".$model->staffcd."','".$model->chaircd."' ) ";     //2005/05/06 賢者-作業依頼書20050506_01
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc020a_1Form1.html", $arg);

        $end = $model->getMicrotime();
        $time = $end - $start;
    }
}
?>
