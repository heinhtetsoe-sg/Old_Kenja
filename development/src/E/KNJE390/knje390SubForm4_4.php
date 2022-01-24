<?php

require_once('for_php7.php');

class knje390SubForm4_4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4_4", "POST", "knje390index.php", "", "subform4_4");

        //DB接続
        $db = Query::dbCheckOut();

        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
        // Add by PP for Title 2020-02-03 start
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = "D 移行支援計画の進路指導計画画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm4_4_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error195= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm4_4_CurrentCursor915\", error195);
              sessionStorage.removeItem(\"KNJE390SubForm4_4_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        /************/
        /* 履歴一覧 */
        /************/
        //進路指導計画の切換
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"RECORD_DIV\" onchange=\"current_cursor('RECORD_DIV'); return btn_submit('subform4_careerguidance')\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje390Query::getCareerguidanceRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $model->field4["RECORD_DIV"], $extra, 1, "");
        
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform4_careerguidance") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
            
            $model->field4["MEETING_NAME"] = "";
            $model->field4["MEETING_DATE"] = "";
            $model->field4["TEAM_MEMBERS"] = "";
            $model->field4["MEETING_SUMMARY"] = "";
            $model->field4["WORK_TRAINING_PLACE"] = "";
            $model->field4["WORK_TRAINING_S_DATE"] = "";
            $model->field4["WORK_TRAINING_E_DATE"] = "";
            $model->field4["WORK_TRAINING_CONTENTS"] = "";
            $model->field4["WORK_TRAINING_GOAL"] = "";
            $model->field4["WORK_TRAINING_SUPPORT"] = "";
            $model->field4["WORK_TRAINING_RESULT"] = "";
            $model->field4["WORK_TRAINING_CHALLENGE"] = "";
            $model->field4["CAREER_GUIDANCE_RESULT"] = "";
            $model->field4["CAREER_GUIDANCE_CHALLENGE"] = "";
            $model->field4["DETERMINED_COURSE"] = "";
            $model->field4["COURSE_CONTENTS"] = "";
            $model->field4["REMARK"] = "";
        }
        if ($model->field4["RECORD_DIV"] !== '5') {
            $arg["RIREKI"] = "1";
            //進路指導計画
            if ($model->cmd == "subform4_careerguidance_set"){
                if (isset($model->schregno) && !isset($model->warning)){
                    $Row = $db->getRow(knje390Query::getSubQuery4CareerguidanceGetData($model), DB_FETCHMODE_ASSOC);
                } else {
                    $Row =& $model->field4;
                }
            } else {
                $Row =& $model->field4;
            }
            //1:相談懇談
            if ($model->field4["RECORD_DIV"] === '1') {
                $arg["RECORD_DIV1"] = "1";
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"会議名\" id=\"MEETING_NAME\"";
                $arg["data"]["MEETING_NAME"] = knjCreateTextBox($objForm, $Row["MEETING_NAME"], "MEETING_NAME", 80, 80, $extra);
                // Add by PP for PC-Talker 2020-02-20 end
                //会議日
                $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
                // Add by PP for PC-Talker 2020-02-03 start
                $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"], "", "会議日");
                // Add by PP for PC-Talker 2020-02-20 end
                //構成員
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "style=\"height:60px; overflow:auto;\" aria-label=\"構成員\"";
                $arg["data"]["TEAM_MEMBERS"] = knjCreateTextArea($objForm, "TEAM_MEMBERS", 4, 21, "soft", $extra, $Row["TEAM_MEMBERS"]);
                $arg["data"]["TEAM_MEMBERS_SIZE"] = '<font size="1" color="red">(全角10文字4行まで)</font>';
                // Add by PP for PC-Talker 2020-02-20 end
                //概要
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "style=\"height:100px; overflow:auto;\" aria-label=\"概要\"";
                $arg["data"]["MEETING_SUMMARY"] = knjCreateTextArea($objForm, "MEETING_SUMMARY", 7, 51, "soft", $extra, $Row["MEETING_SUMMARY"]);
                $arg["data"]["MEETING_SUMMARY_SIZE"] = '<font size="1" color="red">(全角25文字7行まで)</font>';
                // Add by PP for PC-Talker 2020-02-20 end
            //6:決定進路内容(3年次のみ)
            } else if ($model->field4["RECORD_DIV"] === '6') {
                $arg["RECORD_DIV6"] = "1";
                //①事業所、②福祉利用の切換
                $opt = array(1, 2);
                $Row["RECORD_NO"] = ($Row["RECORD_NO"] == "") ? "1" : $Row["RECORD_NO"];
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = array("id=\"RECORD_NO1\" onclick=\"current_cursor('RECORD_NO1'); return btn_submit('subform4_careerguidance')\"", "id=\"RECORD_NO2\" onclick=\"current_cursor('RECORD_NO2');return btn_submit('subform4_careerguidance')\"");
                // Add by PP for PC-Talker 2020-02-20 end
                $radioArray = knjCreateRadio($objForm, "RECORD_NO", $Row["RECORD_NO"], $extra, $opt, get_count($opt));
                foreach($radioArray as $key => $val) $arg["data"][$key] = $val;        
                
                //事業所・福祉利用
                if ($Row["RECORD_NO"] === '1') {
                    $arg["data"]["DETERMINED_NAME"] = '事業所';
                    // Add by PP for PC-Talker 2020-02-03 start
                    $extra = "aria-label=\"事業所\"";
                    // Add by PP for PC-Talker 2020-02-20 end
                } else {
                    $arg["data"]["DETERMINED_NAME"] = '福祉利用';
                    // Add by PP for PC-Talker 2020-02-03 start
                      $extra = "aria-label=\"福祉利用\"";
                    // Add by PP for PC-Talker 2020-02-20 end
                }
                
                $arg["data"]["DETERMINED_COURSE"] = knjCreateTextBox($objForm, $Row["DETERMINED_COURSE"], "DETERMINED_COURSE", 80, 80, $extra);
                //内容
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "style=\"height:60px; overflow:auto;\" aria-label=\"内容全角35文字4行まで\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["COURSE_CONTENTS"] = knjCreateTextArea($objForm, "COURSE_CONTENTS", 4, 71, "soft", $extra, $Row["COURSE_CONTENTS"]);
                $arg["data"]["COURSE_CONTENTS_SIZE"] = '<font size="1" color="red">(全角35文字4行まで)</font>';

                //その他 (固定 RECORD_NO='3' RECORD_SEQ=1のみ)
                if (isset($model->schregno) && !isset($model->warning)){
                    $Row2 = $db->getRow(knje390Query::getSubQuery4CareerguidanceRecordList($model, "6"), DB_FETCHMODE_ASSOC);
                } else {
                    $Row2 =& $model->field4;
                }
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "style=\"height:75px; overflow:auto;\" aria-label=\"その他全角40文字5行まで\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 5, 81, "soft", $extra, $Row2["REMARK"]);
                $arg["data"]["REMARK_SIZE"] = '<font size="1" color="red">(全角40文字5行まで)</font>';

            //2:校内実習、3:職場見学、4:職場実習
            } else {
                //ヘッダー
                if ($model->field4["RECORD_DIV"] === '2') {
                    $arg["data"]["RECORD_NAME"] = '校内実習';
                } else if ($model->field4["RECORD_DIV"] === '3') {
                    $arg["data"]["RECORD_NAME"] = '職場見学';
                } else {
                    $arg["data"]["RECORD_NAME"] = '職場実習';
                }
                $arg["RECORD_DIV234"] = "1";
                //期日開始
                $Row["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $Row["WORK_TRAINING_S_DATE"]);
                // Add by PP for PC-Talker 2020-02-03 start
                $arg["data"]["WORK_TRAINING_S_DATE"] = View::popUpCalendar($objForm, "WORK_TRAINING_S_DATE", $Row["WORK_TRAINING_S_DATE"], "", "期日開始日");
                // Add by PP for PC-Talker 2020-02-20 end
                //期日終了日
                if ($model->field4["RECORD_DIV"] === '2' || $model->field4["RECORD_DIV"] === '4') {
                    $arg["E_DATE"] = "1";
                    $Row["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $Row["WORK_TRAINING_E_DATE"]);
                    // Add by PP for PC-Talker 2020-02-03 start
                    $arg["data"]["WORK_TRAINING_E_DATE"] = View::popUpCalendar($objForm, "WORK_TRAINING_E_DATE", $Row["WORK_TRAINING_E_DATE"], "", "期日終了日");
                    // Add by PP for PC-Talker 2020-02-20 end
                }
                //期日終了日
                if ($model->field4["RECORD_DIV"] === '3' || $model->field4["RECORD_DIV"] === '4') {
                    $arg["PLACE"] = "1";
                    //場所
                    // Add by PP for PC-Talker 2020-02-03 start
                    $extra = "aria-label=\"場所\"";
                     // Add by PP for PC-Talker 2020-02-20 end
                    $arg["data"]["WORK_TRAINING_PLACE"] = knjCreateTextBox($objForm, $Row["WORK_TRAINING_PLACE"], "WORK_TRAINING_PLACE", 40, 40, $extra);
                }
                //支援・手立てのサイズ
                $suportSize = "71";
                $suportSize_html = "35";
                if ($model->field4["RECORD_DIV"] === '3') {
                    $arg["SUPPORT_TARGET"] = "1";
                    //支援・手立て(グループ・個人)
                    $suportSize = "63";
                    $suportSize_html = "31";
                    // Add by PP for PC-Talker 2020-02-03 start
                    $extra = "style=\"height:75px; overflow:auto;\" aria-label=\"支援・手立て全角6文字5行まで\"";
                    // Add by PP for PC-Talker 2020-02-20 end
                    $arg["data"]["WORK_TRAINING_SUPPORT_TARGET"] = knjCreateTextArea($objForm, "WORK_TRAINING_SUPPORT_TARGET", 5, 13, "soft", $extra, $Row["WORK_TRAINING_SUPPORT_TARGET"]);
                    $arg["data"]["WORK_TRAINING_SUPPORT_TARGET_SIZE"] = '<font size="1" color="red">(全角6文字5行まで)</font>';
                }
                
                //内容
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"内容\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["WORK_TRAINING_CONTENTS"] = knjCreateTextBox($objForm, $Row["WORK_TRAINING_CONTENTS"], "WORK_TRAINING_CONTENTS", 80, 80, $extra);
                //目標
                // Add by PP for PC-Talker 2020-02-03 start
                $comment = "全角35文字5行まで";
                $extra = "style=\"height:75px; overflow:auto;\" aria-label=\"目標{$comment}\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["WORK_TRAINING_GOAL"] = knjCreateTextArea($objForm, "WORK_TRAINING_GOAL", 5, 71, "soft", $extra, $Row["WORK_TRAINING_GOAL"]);
                //支援・手立て
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"支援・手立て{$comment}\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["WORK_TRAINING_SUPPORT"] = knjCreateTextArea($objForm, "WORK_TRAINING_SUPPORT", 5, $suportSize, "soft", $extra, $Row["WORK_TRAINING_SUPPORT"]);
                //成果
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"成果{$comment}\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["WORK_TRAINING_RESULT"] = knjCreateTextArea($objForm, "WORK_TRAINING_RESULT", 5, 71, "soft", $extra, $Row["WORK_TRAINING_RESULT"]);
                //課題
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"課題{$comment}\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["WORK_TRAINING_CHALLENGE"] = knjCreateTextArea($objForm, "WORK_TRAINING_CHALLENGE", 5, 71, "soft", $extra, $Row["WORK_TRAINING_CHALLENGE"]);
                $arg["data"]["WORK_TRAINING_SIZE"] = '<font size="1" color="red">(全角35文字5行まで)</font>';
                $arg["data"]["WORK_TRAINING_SUPPORT_SIZE"] = '<font size="1" color="red">(全角'.$suportSize_html.'文字5行まで)</font>';
            }
        //5:年間の成果、課題
        } else {
            $arg["RECORD_DIV5"] = "1";
            if (isset($model->schregno) && !isset($model->warning)){
                $Row3 = $db->getRow(knje390Query::getSubQuery4CareerguidanceResultChallengeGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row3 =& $model->field4;
            }
            //年間の成果
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "style=\"height:75px; overflow:auto;\" aria-label=\"年間の成果全角35文字5行まで\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["data"]["CAREER_GUIDANCE_RESULT"] = knjCreateTextArea($objForm, "CAREER_GUIDANCE_RESULT", 5, 71, "soft", $extra, $Row3["CAREER_GUIDANCE_RESULT"]);
            //年間の課題
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "aria-label=\"年間の課題全角35文字5行まで\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["data"]["CAREER_GUIDANCE_CHALLENGE"] = knjCreateTextArea($objForm, "CAREER_GUIDANCE_CHALLENGE", 5, 71, "soft", $extra, $Row3["CAREER_GUIDANCE_CHALLENGE"]);
            $arg["data"]["CAREER_GUIDANCE_SIZE"] = '<font size="1" color="red">(全角35文字5行まで)</font>';
        }
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm4_4.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $no1Cnt = 1;
    $no2Cnt = 1;
    $query = knje390Query::getSubQuery4CareerguidanceRecordList($model, "");
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field4["RECORD_DIV"] === '1') {
            // Add by PP for PC-Talker 2020-02-03 start
            $rowlist["cursor"] = "MEETING_NAME";
            // Add by PP for PC-Talker 2020-02-20 end
            $rowlist["CONTENTS_NAME"] = '相談懇談'.($retCnt+1);
            $rowlist["MEETING_DATE"] = str_replace("-", "/", $rowlist["MEETING_DATE"]);
            $rowlist["CONTENTS_NAIYOU"] = '会議日:'.$rowlist["MEETING_DATE"].'　概要:'.substr($rowlist["MEETING_SUMMARY"], 0, 120);
        } else if ($model->field4["RECORD_DIV"] === '2') {
            // Add by PP for PC-Talker 2020-02-03 start
            $rowlist["cursor"] = "WORK_TRAINING_S_DATE";
            // Add by PP for PC-Talker 2020-02-20 end
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_E_DATE"]);
            $rowlist["CONTENTS_NAME"] = '校内実習'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'～'.$rowlist["WORK_TRAINING_E_DATE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } else if ($model->field4["RECORD_DIV"] === '3') {
            // Add by PP for PC-Talker 2020-02-03 start
            $rowlist["cursor"] = "WORK_TRAINING_S_DATE";
            // Add by PP for PC-Talker 2020-02-20 end
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["CONTENTS_NAME"] = '職場見学'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'　場所:'.$rowlist["WORK_TRAINING_PLACE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } else if ($model->field4["RECORD_DIV"] === '4') {
            // Add by PP for PC-Talker 2020-02-03 start
            $rowlist["cursor"] = "WORK_TRAINING_S_DATE";
            // Add by PP for PC-Talker 2020-02-20 end
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_E_DATE"]);
            $rowlist["CONTENTS_NAME"] = '職場実習'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'～'.$rowlist["WORK_TRAINING_E_DATE"].'　場所:'.$rowlist["WORK_TRAINING_PLACE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } else if ($model->field4["RECORD_DIV"] === '6') {

            if ($rowlist["RECORD_NO"] === '1') {
                // Add by PP for PC-Talker 2020-02-03 start
                $rowlist["cursor"] = "RECORD_NO1";
                // Add by PP for PC-Talker 2020-02-20 end
                $rowlist["CONTENTS_NAME"] = '事業所'.$no1Cnt;
                $no1Cnt++;
            } else {
                // Add by PP for PC-Talker 2020-02-03 start
                $rowlist["cursor"] = "RECORD_NO2";
                // Add by PP for PC-Talker 2020-02-20 end
                $rowlist["CONTENTS_NAME"] = '福祉利用'.$no2Cnt;
                $no2Cnt++;
            }
            $rowlist["CONTENTS_NAIYOU"] = substr($rowlist["COURSE_CONTENTS"], 0, 120);
        }
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //構成員参照
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_staff\" onclick=\"current_cursor('btn_staff'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=team_member_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "構成員参照", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end

    //追加ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_insert\" aria-label=\"追加\" onclick=\"current_cursor('btn_insert'); return btn_submit('careerguidance4_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update\" aria-label=\"更新\" onclick=\"current_cursor('btn_update'); return btn_submit('careerguidance4_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //削除ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_delete\" aria-label=\"削除\" onclick=\"current_cursor('btn_delete'); return btn_submit('careerguidance4_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    
    //5:年間の成果・課題の更新、6:決定進路内容のその他
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update2\" aria-label=\"更新\" onclick=\"current_cursor('btn_update2'); return btn_submit('careerguidance4_update2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
    //戻るボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "aria-label=\"戻る\" onclick=\"return btn_submit('subform4A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

