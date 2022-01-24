<?php

require_once('for_php7.php');


class knjz220cForm2{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz220cindex.php", "", "edit");

        //DBオープン
        $db = Query::dbCheckOut();

        $subclassname = $db->getOne(knjz220cQuery::getSubclassname($model->field1["SUBCLASSCD"]));

        //ヘッダー設定
        $arg["head"] = array( "SEMESTERNAME"   => $model->semestername,
                              "CODE"           => $model->field1["SUBCLASSCD"],
                              "SUBJECT"        => $subclassname,
                              "GRADE"          => $model->grade_name[$model->grade]);
        
        //デフォルト値を作成
        $default_val = array("ASSESSLOW_ARRAY" => array()
                            ,"ASSESSHIGH_ARRAY" => array()
                            ,"ASSESSMARK_ARRAY" => array()
                            );

        //校種
        $query = knjz220cQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //基本科目 科目名表示
        $setClassDef = "00-".$model->schoolKind."-00-000000";
        if($setClassDef == $model->field1["SUBCLASSCD"]){
            $arg["head"]["SUBJECT"] = "基本科目";
        }

        $query = knjz220cQuery::getDefaultData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //小数点を取り除く。
            list($row["ASSESSLOW"]) = explode(".", $row["ASSESSLOW"]);
            list($row["ASSESSHIGH"]) = explode(".", $row["ASSESSHIGH"]);

            $default_val["ASSESSLOW_ARRAY"][] = $row["ASSESSLOW"];
            $default_val["ASSESSHIGH_ARRAY"][] = $row["ASSESSHIGH"];
            $default_val["ASSESSMARK_ARRAY"][] = $row["ASSESSMARK"];
        }
        //最小値
        $default_val["MIN"]  = $default_val["ASSESSLOW_ARRAY"][0];
        //最大値
        $default_val["MAX"]  = $default_val["ASSESSHIGH_ARRAY"][get_count($default_val["ASSESSHIGH_ARRAY"]) - 1];

        $default_val["COUNT"] = get_count($default_val["ASSESSLOW_ARRAY"]);

        $isQuery = false;
        if (!isset($model->warning)) {
            $rows = array_map(function($row) {
                                    array_walk($row, "htmlspecialchars_array");
                                    //小数点を取り除く。
                                    list($row["ASSESSLOW"]) = explode(".", $row["ASSESSLOW"]);
                                    list($row["ASSESSHIGH"]) = explode(".", $row["ASSESSHIGH"]);
                                    return $row;
                                }, knjz220cModel::fetchRows(knjz220cQuery::selectQuery($model), $db));
            if ($model->assesslevelcnt == '' &&get_count($rows)) {
                $model->assesslevelcnt = get_count($rows);
            }
            $isQuery = true;
        } else {
            $row    =& $model->field2;
        }
        if ($model->assesslevelcnt == '') {
            $model->assesslevelcnt = $default_val["COUNT"];
        }
        $up = "";
        if ($model->copy["FLG"] == true) {
            $up = $model->field1["UPDATED"];
        } else if ($isQuery) {
            $up = implode(",", array_map(function($row) { return $row["UPDATED"]; }, $rows));
        } else {
            $up = $model->field1["UPDATED"];
        }

        //段階数
        $extra = " style=\"text-align: right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $model->assesslevelcnt, "ASSESSLEVELCNT", 4, 3, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$model->assesslevelcnt.");\" ";
        $arg["sepa"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $extra);

        //テーブルテンプレート作成。
        $tmp_JScript  = " STYLE=\"text-align: right\" "
                      . " onChange=\"document.getElementById('strID%s').innerHTML = (this.value - 1);\" "
                      . " onblur=\"this.value=isNumb(this.value), cleaning_val('off')\" "
                      ;
        //テーブル作成
        for ($i = 1; $i <= $model->assesslevelcnt; $i++) {
            if ($isQuery) {
                $row = $rows[$i - 1];
            }

            $row["ASSESSLEVEL"] = $i;

            if ($model->cmd == 'setdef') {
                $row["ASSESSMARK"] = $default_val["ASSESSMARK_ARRAY"][$i - 1];
            } else if ($isQuery) {
                $row["ASSESSMARK"] = isset($row["ASSESSMARK"]) ? $row["ASSESSMARK"] : "" ;
            } else {
                $row["ASSESSMARK"] = $row["ASSESSMARK".$i];
            }

            //評定名称テキストボックス作成(ASSESSMARK)
            $extra = " STYLE=\"text-align: center\" onblur=\"this.value=toAlphaNumber(this.value)\"; ";
            $row["ASSESSMARKTEXT"] = knjCreateTextBox($objForm, $row["ASSESSMARK"], "ASSESSMARK".$i, 4, 2, $extra);

            //下限値テキストの有無設定
            if ($row["ASSESSLEVEL"] == 1) {
                $row["ASSESSLOWTEXT"] = $default_val["MIN"];
            } else {
                $textvalue = "";
                if ($model->cmd == 'setdef') {
                    $textvalue = $default_val["ASSESSLOW_ARRAY"][$i - 1];
                } else if ($model->cmd != 'new') {
                    if ($isQuery) {
                        $textvalue  = isset($row["ASSESSLOW"]) ? $row["ASSESSLOW"] : "" ;
                    } else {
                        $textvalue  = $row["ASSESSLOW".$i];
                    }
                }

                //テキストボックス作成(ASSESSLOW)
                $extra = sprintf($tmp_JScript, ($i - 1));
                $row["ASSESSLOWTEXT"] = knjCreateTextBox($objForm, $textvalue, "ASSESSLOW".$i, 4, 2, $extra);
            }

            //非text部分作成
            if ($row["ASSESSLEVEL"] == $model->assesslevelcnt) {
                $textvalue = "";
                if ($model->cmd == 'setdef') {
                    $textvalue = $default_val["ASSESSHIGH_ARRAY"][$i - 1];
                } else if ($model->cmd != 'new') {
                    if ($isQuery) {
                        $textvalue  = isset($row["ASSESSHIGH"]) ? $row["ASSESSHIGH"] : "" ;
                    } else {
                        $textvalue  = $row["ASSESSHIGH".$i];
                    }
                }
                //テキストボックス作成(ASSESSHIGH)
                $extra = '';
                $row["ASSESSHIGHTEXT"] = knjCreateTextBox($objForm, $textvalue, "ASSESSHIGH", 4, 3, $extra);
            } else {
                $tablevalue = "";
                if ($model->cmd != 'new') {
                    if ($model->cmd == 'setdef') {
                        $tablevalue = $default_val["ASSESSHIGH_ARRAY"][$i - 1];
                    } else if ($isQuery) {
                        $tablevalue = isset($row["ASSESSHIGH"]) ? $row["ASSESSHIGH"] : "";
                    } else {
                        $tablevalue = ($row["ASSESSLOW".($i + 1)] - 1);
                    }
                }
                $row["ASSESSHIGHTEXT"] = "<span id=\"strID".$row["ASSESSLEVEL"]."\">".$tablevalue."</span>";
            }

            $arg["data"][] = $row;
        }

        //コピー機能コンボボックス内データ取得
        $query = knjz220cQuery::Copy_comboQuery($model->grade, $model);
        $extra = "";
        $arg["head"]["copy_cmb"] = knjCreateCombo($objForm, "COPY_SELECT", $model->copy["SELECT"], knjz220cModel::createOpts($query, $db), $extra, 1);
        $extra = "style=\"width:200px\"onclick=\"return btn_submit('copy')\"";
        $arg["head"]["BUTTON"] = knjCreateBtn($objForm, "btn_copy", "左の科目の評定をコピー", $extra);
        Query::dbCheckIn($db);
                    
        //デフォルトに戻すボタン
        $extra = "onclick=\"return btn_submit('setdef');\"";
        $arg["button"]["btn_def"] = knjCreateBtn($objForm, "btn_def", "デフォルトに戻す", $extra);

        //更新ボタン
        $extra = " onclick=\"return btn_submit('update'), cleaning_val('off');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"" ;
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SUBCLASSCD", $model->field1["SUBCLASSCD"]);
        //テーブルの作成数
        knjCreateHidden($objForm, "TBL_COUNT", $default_val["COUNT"]);
        //データ処理日の保持
        knjCreateHidden($objForm, "UPDATED", $up);
        knjCreateHidden($objForm, "Cleaning", $model->Clean);
        //デフォルト値
        knjCreateHidden($objForm, "default_val_low", implode(',', $default_val["ASSESSLOW_ARRAY"]));
        knjCreateHidden($objForm, "default_val_high", implode(',', $default_val["ASSESSHIGH_ARRAY"]));
        knjCreateHidden($objForm, "default_val_mark", implode(',', $default_val["ASSESSMARK_ARRAY"]));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz220cForm2.html", $arg); 

    }
}
?>
