<?php

require_once('for_php7.php');
class knjxattend3Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //生徒情報取得+ヘッダー作成
        $r_stuInfo = $db->getRow(knjxattend3Query::getStudentInfo($model),DB_FETCHMODE_ASSOC); 
        $arg["HEADERS"] = $r_stuInfo;

        //年度の初期化
        if (!$model->grade_combo) {
            $model->grade_combo = $model->exp_year;
        }
        
        //コンボボックス値作成
        $result = $db->query(knjxattend3Query::getYear($model)); 
        $opt = array();
        $check_val = "";
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if($check_val == "" && $model->grade_combo != "") {
                $check_val = ($model->grade_combo == $row["YEAR"])? "check" : "" ;
            }
        }

        if($model->grade_combo == "" || $check_val == "" ){
            $model->grade_combo = (isset($opt[0]["label"]))? $opt[0]["label"] : "" ;
        }
        
        if($model->prgid == 'KNJE020') {
            //年度コンボボックス
            $extra = "onchange=\"return btn_submit('');\"";
            $setYear = knjCreateCombo($objForm, "GRADE_COMBO", $model->grade_combo, $opt, $extra, 1);
            $arg["GRADEITEM"] = array( "GRADE_COMBO" => $setYear);
            
        } else {
            //年度コンボボックス
            $extra = "onchange=\"return btn_submit('');\"";
            $setYear = knjCreateCombo($objForm, "GRADE_COMBO", $model->grade_combo, $opt, $extra, 1);
                                
            //年度入力テキスト
            $extra = "onblur=\"this.value=toInteger(this.value);\"";
            $setYearAdd = knjCreateTextBox($objForm, "", "GRADE_TXT", 5, 4, $extra);

            //年度追加ボタン
            $extra = "onclick=\"return add('');\"";
            $setYearBtn = knjCreateBtn($objForm, "GRADE_BTN", "年度追加", $extra);

            $arg["GRADEITEM"]  = array( "GRADE_COMBO" => $setYear."&nbsp;".
                                                         $setYearAdd."&nbsp;".
                                                         $setYearBtn );
        }

        $Row = array();
        $result = $db->query(knjxattend3Query::getSchregAttendrecDat_Val($model));

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
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

        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->field;
        }

        //初期化
        //$model->field["GRADE"] = $model->field["SCHOOLCD"] = $model->field["YEAR"] = array();

        //京都（通信制）フラグ
        if ($model->isKyotoTushin == "1") {
            $disabledKyoto = " disabled";
        } else if ($model->isNichiNi == "1") {
            $readOnlyNichiNi = " readOnly ";
        }

        if($model->prgid == 'KNJE020') {
            for($i=0; $i<2; $i++)
            {
                $javascript = " onblur=\"sum(this,'".$i."')\"; ";
                $arg["ID".$i] = $i;

                //年次
                $arg["ANNUAL".$i] = $Row["ANNUAL"][$i];

                //授業日数
                $arg["CLASSDAYS".$i] = $Row["CLASSDAYS"][$i];

                //転入生出欠データ入力
                //2.休学日数
                $arg["OFFDAYS".$i] = $Row["OFFDAYS"][$i];

                //3.出席停止の日数
                $arg["SUSPEND".$i] = $Row["SUSPEND"][$i];

                //4.忌引きの日数
                $arg["MOURNING".$i] = $Row["MOURNING"][$i];

                //5.留学中の授業日数
                $arg["ABROAD".$i] = $Row["ABROAD"][$i];

                //6.出席しなければならない日数
                $arg["REQUIREPRESENT".$i] = ((int)$Row["CLASSDAYS"][$i] - (int)$Row["OFFDAYS"][$i]) - 
                                     ((int)$Row["SUSPEND"][$i] + (int)$Row["MOURNING"][$i] + (int)$Row["ABROAD"][$i]);

                //7.病欠
                $arg["SICK".$i] = $Row["SICK"][$i];

                //8.事故欠(届)
                $arg["ACCIDENTNOTICE".$i] = $Row["ACCIDENTNOTICE"][$i];

                //9.事故欠(無)
                $arg["NOACCIDENTNOTICE".$i] = $Row["NOACCIDENTNOTICE"][$i];

                //10.出席日数
                $arg["PRESENT".$i] = (int)$arg["REQUIREPRESENT".$i] - 
                                  ((int)$Row["SICK"][$i] + (int)$Row["ACCIDENTNOTICE"][$i] + (int)$Row["NOACCIDENTNOTICE"][$i]);

                if(isset($scd)){
                    //hiddenを作成する
                    $objForm->ae( array("type"     => "hidden",
                                        "name"     => "SCHOOLCD".$i,
                                        "value"    => (isset($Row["SCHOOLCD"][$i])? $Row["SCHOOLCD"][$i] : "") ));

                    $arg["SCHOOLCD".$i] = $objForm->ge("SCHOOLCD".$i);
                }
            }
        } else {
            for($i=0; $i<2; $i++)
            {
                $javascript = " onblur=\"sum(this,'".$i."')\"; ";
                $arg["ID".$i] = $i;

                //通信制フラグ(「1:前校」はグレーアウトしない)
                if ($i == "1" && $model->isTushin == "1") {
                    $disabledKyoto = "";
                }

                //年次
                $extra = " STYLE=\"text-align:right;\" onblur=\"checkValue(this);\" ";
                $arg["ANNUAL".$i] = knjCreateTextBox($objForm, $Row["ANNUAL"][$i], "ANNUAL".$i, 5, 2, $extra);

                //授業日数                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["CLASSDAYS".$i] = knjCreateTextBox($objForm, $Row["CLASSDAYS"][$i], "CLASSDAYS".$i, 5, 3, $extra);

                //転入生出欠データ入力
                //2.休学日数                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["OFFDAYS".$i] = knjCreateTextBox($objForm, $Row["OFFDAYS"][$i], "OFFDAYS".$i, 5, 3, $extra);

                //3.出席停止の日数
                $gray = ($model->isNichiNi == "1") ? " background-color:gray; " : "";
                $extra = " STYLE=\"text-align:right;{$gray}\" ".$javascript.$disabledKyoto.$readOnlyNichiNi;
                $arg["SUSPEND".$i] = knjCreateTextBox($objForm, $Row["SUSPEND"][$i], "SUSPEND".$i, 5, 3, $extra);

                //4.忌引きの日数                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["MOURNING".$i] = knjCreateTextBox($objForm, $Row["MOURNING"][$i], "MOURNING".$i, 5, 3, $extra);

                //5.留学中の授業日数                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["ABROAD".$i] = knjCreateTextBox($objForm, $Row["ABROAD"][$i], "ABROAD".$i, 5, 3, $extra);

                //6.出席しなければならない日数
                $arg["REQUIREPRESENT".$i] = ( (int)$Row["CLASSDAYS"][$i] - (int)$Row["OFFDAYS"][$i]) - 
                                     ((int)$Row["SUSPEND"][$i] + (int)$Row["MOURNING"][$i] + (int)$Row["ABROAD"][$i]);

                //7.病欠                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["SICK".$i] = knjCreateTextBox($objForm, $Row["SICK"][$i], "SICK".$i, 5, 3, $extra);

                //8.事故欠(届)                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["ACCIDENTNOTICE".$i] = knjCreateTextBox($objForm, $Row["ACCIDENTNOTICE"][$i], "ACCIDENTNOTICE".$i, 5, 3, $extra);

                //9.事故欠(無)                
                $extra = " STYLE=\"text-align:right;\" ".$javascript.$disabledKyoto;
                $arg["NOACCIDENTNOTICE".$i] = knjCreateTextBox($objForm, $Row["NOACCIDENTNOTICE"][$i], "NOACCIDENTNOTICE".$i, 5, 3, $extra);

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
        }

        if($model->prgid == 'KNJE020') {
            //学籍番号チェック
            if (!$model->schregno) {
                $arg["check_schregno"] = "checkSchregno();";
            }
            //戻るボタンを作成する
            $extra = "onclick=\"return parent.closeit()\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        } else {
            //更新ボタンを作成する
            $extra = "onclick=\"return btn_submit('update2');\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $extra);

            //取消ボタンを作成する
            $extra = "onclick=\"return ShowConfirm();\"";
            $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra);

            //終了ボタンを作成する
            $extra = "onclick=\"return closeWin();\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
        }

        //履歴表示
        if ($model->prgid == 'KNJE041') {
            $arg["useRireki"] = "1";
            makeListRireki($objForm, $db, $arg, $model);
        }

        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_add",
                            "value"      => "off" ));

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
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno));

        //hiddenを作成
        knjCreateHidden($objForm, "PRGID", $model->prgid);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxattend3Form1.html", $arg);
    }
}

//履歴表示
function makeListRireki(&$objForm, $db, &$arg, $model) {
    $lineKeyPre = "";   //前データ(1行表示)
    $firstflg = true;   //初回フラグ
    $setval = array();  //出力データ配列
    if (isset($model->schregno)) {
        $query = knjxattend3Query::getListRireki($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);

            $lineKey = $row["CALC_DATE"]."-".$row["CALC_TIME"]."-".$row["YEAR"];
            if ($firstflg || ($lineKey == $lineKeyPre)) {
                //同一処理日時、年度のデータを連結(初回データ設定も含む)
                $setval = setData($row, $setval, $firstflg, "join");
                $firstflg = false;
            } else {
                //前データ出力
                $arg["dataR"][] = $setval;
                $setval = array();
                //現データ設定
                $setval = setData($row, $setval, $firstflg);
            }
            $lineKeyPre = $lineKey;
        }
        $result->free();

        if (!$firstflg) {
            $arg["dataR"][] = $setval;
        }
    }
}

//
function setData($row, &$setval, $firstflg, $join = "") {
    if ($firstflg || $join != "join") {
        $setval = $row;
    }
    $scd = $row["SCHOOLCD"]."_".$row["BEF_AFT_DIV"];
    $setval["ANNUAL".$scd]           = $row["ANNUAL"];           //年次
    $setval["CLASSDAYS".$scd]        = $row["CLASSDAYS"];        //授業日数
    $setval["OFFDAYS".$scd]          = $row["OFFDAYS"];          //休学日数
    $setval["SUSPEND".$scd]          = $row["SUSPEND"];          //出席停止の日数
    $setval["MOURNING".$scd]         = $row["MOURNING"];         //忌引きの日数
    $setval["ABROAD".$scd]           = $row["ABROAD"];           //留学中の授業日数
//    $setval["REQUIREPRESENT".$scd]   = $row["REQUIREPRESENT"];   //出席しなければならない日数
    $setval["SICK".$scd]             = $row["SICK"];             //病欠
    $setval["ACCIDENTNOTICE".$scd]   = $row["ACCIDENTNOTICE"];   //事故欠(届)
    $setval["NOACCIDENTNOTICE".$scd] = $row["NOACCIDENTNOTICE"]; //事故欠(無)
//    $setval["PRESENT".$scd]          = $row["PRESENT"];          //出席日数
    $setval["COLOR"] = "white";

    return $setval;
}
?>