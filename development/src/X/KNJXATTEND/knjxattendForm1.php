<?php

require_once('for_php7.php');

class knjxattendForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        $query  = knjxattendQuery::getZ010("00");
        $result = $db->query($query);
        $isTushin = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $isTushin = "1" == $row["NAMESPARE3"] ? true : false;
        }
        $result->free();

        //生徒情報取得+ヘッダー作成
        $r_stuInfo = $db->getRow(knjxattendQuery::getStudentInfo($model),DB_FETCHMODE_ASSOC);
        $arg["HEADERS"] = $r_stuInfo;

        //コンボボックス値作成
        $model->grade_combo = $model->grade_combo ? $model->grade_combo : $model->exp_year;
        $result = $db->query(knjxattendQuery::getYear($model));
        $opt = array();
        $check_val = "";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if($check_val == "" && $model->grade_combo != ""){
                $check_val = ($model->grade_combo == $row["YEAR"])? "check" : "" ;
            }
        }

        if($model->grade_combo == "" || $check_val == "" ){
            $model->grade_combo = (isset($opt[0]["label"]))? $opt[0]["label"] : "" ;
        }

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["GRADEITEM"]["GRADE_COMBO"] = knjCreateCombo($objForm, "GRADE_COMBO", $model->grade_combo, $opt, $extra, 1);

        //年度入力テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["GRADEITEM"]["GRADE_TXT"] = knjCreateTextBox($objForm, "", "GRADE_TXT", 5, 4, $extra);

        //年度追加ボタン
        $arg["GRADEITEM"]["GRADE_BTN"] = knjCreateBtn($objForm, "GRADE_BTN", "年度追加", "onclick=\"return add('');\"");

        $query = knjxattendQuery::getHexamEntremarkDat_Val($model);
        $memo = $db->getRow($query,DB_FETCHMODE_ASSOC);

        $Row = array();
        $result = $db->query(knjxattendQuery::getSchregAttendrecDat_Val($model));

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["SCHOOLCD"];

            $Row["YEAR"][$scd]             = $row["YEAR"];             //年度
            $Row["ANNUAL"][$scd]           = $row["ANNUAL"];           //年次
            $Row["SCHOOLCD"][$scd]         = $row["SCHOOLCD"];         //前校(cd:0)/本校(cd:1)
            $Row["CLASSDAYS"][$scd]        = $row["CLASSDAYS"];        //授業日数
            $Row["CLASSDAYS"][$scd]        = $row["CLASSDAYS"];        //授業日数
            $Row["OFFDAYS"][$scd]          = $row["OFFDAYS"];          //休学日数
            $Row["SUSPEND"][$scd]          = $row["SUSPEND"];          //出席停止の日数
            $Row["MOURNING"][$scd]         = $row["MOURNING"];         //忌引きの日数
            $Row["ABROAD"][$scd]           = $row["ABROAD"];           //留学中の授業日数
            $Row["REQUIREPRESENT"][$scd]   = $row["REQUIREPRESENT"];   //出席しなければならない日数
            $Row["SICK"][$scd]             = $row["SICK"];             //病欠
            $Row["ACCIDENTNOTICE"][$scd]   = $row["ACCIDENTNOTICE"];   //事故欠(届)
            $Row["NOACCIDENTNOTICE"][$scd] = $row["NOACCIDENTNOTICE"]; //事故欠(無)
            $Row["PRESENT"][$scd]          = $row["PRESENT"];          //出席日数
            $Row["UPDATED_ATTEND"][$scd]   = $row["UPDATED"];          //変更日時
        }

        $Row["ATTENDREC_REMARK"] = $memo["ATTENDREC_REMARK"];        //備考(knje040は除く)
        $Row["UPDATED_MEMO"]     = $memo["UPDATED"];                 //備考変更日時

        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)) {
            $Row =& $model->field;
        }

        //京都（通信制）フラグ
        if ($model->isKyotoTushin == "1") {
            $disabledKyoto = " disabled";
        }

        for($i=0; $i<2; $i++) {
            $javascript = " onblur=\"sum(this,'".$i."')\"; ";
            $arg["ID".$i] = $i;

            //年次
            $objForm->ae( array("type"      => "text",
                                "name"      => "ANNUAL".$i,
                                "size"      => 5,
                                "maxlength" => 2,
                                "value"     => $Row["ANNUAL"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" onblur=\"checkValue(this);\" ") );

            $arg["ANNUAL".$i] = $objForm->ge("ANNUAL".$i);

            //授業日数
            $objForm->ae( array("type"      => "text",
                                "name"      => "CLASSDAYS".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["CLASSDAYS"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["CLASSDAYS".$i] = $objForm->ge("CLASSDAYS".$i);

            //転入生出欠データ入力
            //2.休学日数
            $objForm->ae( array("type"      => "text",
                                "name"      => "OFFDAYS".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["OFFDAYS"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["OFFDAYS".$i] = $objForm->ge("OFFDAYS".$i);

            //3.出席停止の日数
            $objForm->ae( array("type"      => "text",
                                "name"      => "SUSPEND".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["SUSPEND"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["SUSPEND".$i] = $objForm->ge("SUSPEND".$i);

            //4.忌引きの日数
            $objForm->ae( array("type"      => "text",
                                "name"      => "MOURNING".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["MOURNING"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["MOURNING".$i] = $objForm->ge("MOURNING".$i);

            //5.留学中の授業日数
            $objForm->ae( array("type"      => "text",
                                "name"      => "ABROAD".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["ABROAD"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["ABROAD".$i] = $objForm->ge("ABROAD".$i);

            //6.出席しなければならない日数
            $arg["REQUIREPRESENT".$i] = ( (int)$Row["CLASSDAYS"][$i] - (int)$Row["OFFDAYS"][$i]) -
                                 ((int)$Row["SUSPEND"][$i] + (int)$Row["MOURNING"][$i] + (int)$Row["ABROAD"][$i]);

            //7.病欠
            $objForm->ae( array("type"      => "text",
                                "name"      => "SICK".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["SICK"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["SICK".$i] = $objForm->ge("SICK".$i);

            //8.事故欠(届)
            $objForm->ae( array("type"      => "text",
                                "name"      => "ACCIDENTNOTICE".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["ACCIDENTNOTICE"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["ACCIDENTNOTICE".$i] = $objForm->ge("ACCIDENTNOTICE".$i);

            //9.事故欠(無)
            $objForm->ae( array("type"      => "text",
                                "name"      => "NOACCIDENTNOTICE".$i,
                                "size"      => 5,
                                "maxlength" => 3,
                                "value"     => $Row["NOACCIDENTNOTICE"][$i],
                                "extrahtml" => " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto) );

            $arg["NOACCIDENTNOTICE".$i] = $objForm->ge("NOACCIDENTNOTICE".$i);

            //10.出席日数
            if ($model->isKyotoTushin == "1") {
                $extra = "STYLE=\"text-align:right;\" onblur=\"checkValue(this);\"";
                $arg["PRESENT".$i] = knjCreateTextBox($objForm, $Row["PRESENT"][$i], "PRESENT".$i, 5, 3, $extra);
            } else {
                $arg["PRESENT".$i] = (int)$arg["REQUIREPRESENT".$i] -
                                  ((int)$Row["SICK"][$i] + (int)$Row["ACCIDENTNOTICE"][$i] + (int)$Row["NOACCIDENTNOTICE"][$i]);
            }

            if(isset($scd)){
                //hiddenを作成する
                $objForm->ae( array("type"     => "hidden",
                                    "name"     => "SCHOOLCD".$i,
                                    "value"    => (isset($Row["SCHOOLCD"][$i])? $Row["SCHOOLCD"][$i] : "") ));

                $arg["SCHOOLCD".$i] = $objForm->ge("SCHOOLCD".$i);
            }
        }

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update2');\"";
        if ($model->memo_flg == 'knje040' && $isTushin && $model->isKyotoTushin == "") {
            $extra .= " disabled";
        }
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更新",
                            "extrahtml" => $extra));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //取消ボタンを作成する
        $objForm->ae( array("type"      => "reset",
                            "name"      => "btn_reset",
                            "value"     => "取消",
                            "extrahtml" => "onclick=\"return ShowConfirm();\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        if ($model->programid == "KNJE010A") {
            //転入生出欠データ入力
            if ($model->memo_flg != "unset"){
                //備考
                $objForm->ae( array("type"      => "textarea",
                                    "name"      => "attendrec_remark",
                                    "rows"      => "3",
                                    "cols"      => "11",
                                    "maxlength" => 15,
                                    "extrahtml" => "style=height:48px",
                                    "wrap"      => "hard",
                                    "value"     => $Row["ATTENDREC_REMARK"]
                            ));
                $arg["attendrec"]["remark"] = $objForm->ge("attendrec_remark");
            }

            $value = "戻る";
            $end = "onclick=\"return top.main_frame.right_frame.closeit()\"";
            knjCreateHidden($objForm, "PROGRAMID", "KNJE010A");
        } else {
            $value = "終了";
            $end = "onclick=\"return closeWin();\"";
        }


        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => $value,
                            "extrahtml" => $end));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_add",
                            "value"     => "off" ));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED_ATTEND0",
                            "value"     => $Row["UPDATED_ATTEND"][0] ));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED_ATTEND1",
                            "value"     => $Row["UPDATED_ATTEND"][1] ));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED_MEMO",
                            "value"     => $Row["UPDATED_MEMO"] ));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno));

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MEMO",
                            "value"     => $model->memo_flg));


        $arg["finish"] = $objForm->get_finish();

        Query::dbCheckIn($db);

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattendForm1.html", $arg);
    }
}
?>
