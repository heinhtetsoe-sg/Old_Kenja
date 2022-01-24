<?php

require_once('for_php7.php');

class knjc030_1Form1
{
    function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc030_1index.php", "", "edit");

        //dbopen
        $db = Query::dbCheckOut();

        $thisMonth = array();
        $thisMonth = explode("-",$model->executedate);

        //学期取得
//        $query = knjc030_1Query::getTerm($thisMonth[1]);
        $query = knjc030_1Query::getTerm($model->executedate);
        $model->semester = $db->getOne($query);

        //講座情報取得
        $query = knjc030_1Query::getTargetChair($model);
        $result = $db->query($query);
        //コンボボックス用データ
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
           $opt[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                          "value" => $row["CHAIRCD"]."-".$row["PERIODCD"]); //2006.02.04 alp m-yama
        }

        if($model->chaircd == "" || is_null($model->chaircd)){
            $cha = explode('-',$opt[0]["value"]);   //2006.02.04 alp m-yama
            $model->chaircd = $cha[0];              //2006.02.04 alp m-yama
            $model->pericd  = $cha[1];              //2006.02.04 alp m-yama
        }
        //メインデータ取得
        $query = knjc030_1Query::getTimeTable($model);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
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
            $arg["TRGTCLASS"][$row["TARGETCLASS"]] = $row["LESSON_NAME"].$row["TARGETCLASS"];
            //講座名
            $arg["CHAIRNAME"] = " ".$row["CHAIRCD"]."  ".$row["CHAIRNAME"];
            //出欠済み/出欠未
            $arg["EXECUTED"] = ($row["EXECUTED"] == "1" )? "済み" : "未" ;
            //確認者
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["ATTESTOR"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $row["FUSE_ATTESTOR"] = $ume.substr($row["ATTESTOR"], (strlen($row["ATTESTOR"]) - (int)$simo), (int)$simo);
            } else {
                $row["FUSE_ATTESTOR"] = $row["ATTESTOR"];
            }
            $arg["FUSE_ATTESTOR"] = " ".$row["FUSE_ATTESTOR"]."  ".$row["ATTESTOR_NAME"];
        }

        //スタッフを整列
        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        foreach( $arg["STAFF"] as $key => $val) {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($key) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $key = $ume.substr($key, (strlen($key) - (int)$simo), (int)$simo);
            }
            if(isset($arg["STAFFNAME"])) {
                $arg["STAFFNAME"] .= ", ".$key." ".$val;
            } else {
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
        //施設を整列
        foreach( $arg["TRGTCLASS"] as $key => $val){
            if(isset($arg["TRGTCLASSNAME"])){
                $arg["TRGTCLASSNAME"] .= ", ".$val;
            }else{
                $arg["TRGTCLASSNAME"] = $val;
            }
        }
        //科目を整列
        foreach( $arg["SUBCLASS"] as $key => $val){
            if(isset($arg["SUBCLASS_SHOW"])){
                $arg["SUBCLASS_SHOW"] .= ", ".$key." ".$val;
            }else{
                $arg["SUBCLASS_SHOW"] = $key." ".$val;
            }
        }

        //担任コード１取得
        $tr_cd1 = $db->getOne(knjc030_1Query::getTr_cd1($model));
        //DBクローズ
        $result->free();
        Query::dbCheckIn($db);

        if(get_count($opt) > 1){
            //科目コンボボックスを作成する
            $objForm->ae( array("type"      => "select",
                                "name"      => "chaircombo",
                                "size"      => "1",
                                "value"     => $model->chaircd."-".$model->pericd,  //2006.02.04 alp m-yama
                                "options"   => $opt,
                                "extrahtml" => "onChange=\"btn_submit('');\";" ) );

            $arg["chaircombo"] = $objForm->ge("chaircombo");
        }else{
            $arg["chaircombo"] = $arg["CHAIRNAME"];
        }

        //戻るボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => " 戻 る ",
                            "extrahtml" => " onclick=\"return closeWindow();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");
        
        //出欠入力画面へボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_jump",
                            "value"     => " 出欠入力画面へ ",
                            "extrahtml" => " onclick=\"return btn_submit('jmp');\"" ) );

        $arg["btn_jump"] = $objForm->ge("btn_jump");

        //hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mode",
                            "value"     => $model->adminMode ) );

        if($model->cmd == "jmp"){


            //リンク先作成
#            $db    = Query::dbCheckOut();
#            $link  = $db->getOne(knjc030_1Query::getPath());
            $db    = Query::dbCheckOut();
            $pericdcd  = $db->getOne(knjc030_1Query::getPericdcd($model->executedate,$model->chaircd));

            $link  = "/C/KNJC010";
            $link  = REQUESTROOT.$link."/knjc010index.php";
            Query::dbCheckIn($db);

#            $arg["jscp"] =" IsUserOK_ToJump('".$link."','".$model->executedate."','1','".$model->t_Staffcd."','".$model->chaircd."') ";
#            $arg["jscp"] =" IsUserOK_ToJump('".$link."','".$model->executedate."','1','".STAFFCD."','".$model->chaircd."','".$tr_cd1."') ";
            $arg["jscp"] =" IsUserOK_ToJump('".$link."','".$model->executedate."','".$pericdcd."','".STAFFCD."','".$model->chaircd."','".$tr_cd1."') ";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030_1Form1.html", $arg);
    }
}
?>
