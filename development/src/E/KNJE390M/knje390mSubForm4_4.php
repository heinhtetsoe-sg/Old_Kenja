<?php

require_once('for_php7.php');
class knje390mSubForm4_4
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4_4", "POST", "knje390mindex.php", "", "subform4_4");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* 履歴一覧 */
        /************/
        //進路指導計画の切換
        $extra = "onchange=\"return btn_submit('subform4_careerguidance')\"";
        $query = knje390mQuery::getCareerguidanceRecordDiv();
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
            if ($model->cmd == "subform4_careerguidance_set") {
                if (isset($model->schregno) && !isset($model->warning)) {
                    $Row = $db->getRow(knje390mQuery::getSubQuery4CareerguidanceGetData($model), DB_FETCHMODE_ASSOC);
                } else {
                    $Row =& $model->field4;
                }
            } else {
                $Row =& $model->field4;
            }
            //1:相談懇談
            if ($model->field4["RECORD_DIV"] === '1') {
                $arg["RECORD_DIV1"] = "1";
                $extra = "";
                $arg["data"]["MEETING_NAME"] = knjCreateTextBox($objForm, $Row["MEETING_NAME"], "MEETING_NAME", 80, 80, $extra);
                $arg["data"]["MEETING_NAME_SIZE"] = '<font size="2" color="red">(全角40文字まで)</font>';
                //会議日
                $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
                $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"]);
                //構成員
                $extra = "style=\"height:60px; overflow:auto;\"";
                // $arg["data"]["TEAM_MEMBERS"] = knjCreateTextArea($objForm, "TEAM_MEMBERS", 4, 21, "soft", $extra, $Row["TEAM_MEMBERS"]);
                $arg["data"]["TEAM_MEMBERS"] = getTextOrArea($objForm, "TEAM_MEMBERS", 10, 4, $Row["TEAM_MEMBERS"]);
                $arg["data"]["TEAM_MEMBERS_SIZE"] = '<font size="2" color="red">(全角10文字X4行まで)</font>';
                //概要
                $extra = "style=\"height:100px; overflow:auto;\"";
                // $arg["data"]["MEETING_SUMMARY"] = knjCreateTextArea($objForm, "MEETING_SUMMARY", 7, 51, "soft", $extra, $Row["MEETING_SUMMARY"]);
                $arg["data"]["MEETING_SUMMARY"] = getTextOrArea($objForm, "MEETING_SUMMARY", 25, 7, $Row["MEETING_SUMMARY"]);
                $arg["data"]["MEETING_SUMMARY_SIZE"] = '<font size="2" color="red">(全角25文字X7行まで)</font>';
            //6:決定進路内容(3年次のみ)
            } elseif ($model->field4["RECORD_DIV"] === '6') {
                $arg["RECORD_DIV6"] = "1";
                //①事業所、②福祉利用の切換
                $opt = array(1, 2);
                $Row["RECORD_NO"] = ($Row["RECORD_NO"] == "") ? "1" : $Row["RECORD_NO"];
                $extra = array("id=\"RECORD_NO1\" onclick=\"return btn_submit('subform4_careerguidance')\"", "id=\"RECORD_NO2\" onclick=\"return btn_submit('subform4_careerguidance')\"");
                $radioArray = knjCreateRadio($objForm, "RECORD_NO", $Row["RECORD_NO"], $extra, $opt, get_count($opt));
                foreach ($radioArray as $key => $val) {
                    $arg["data"][$key] = $val;
                }
                
                //事業所・福祉利用
                if ($Row["RECORD_NO"] === '1') {
                    $arg["data"]["DETERMINED_NAME"] = '事業所';
                } else {
                    $arg["data"]["DETERMINED_NAME"] = '福祉利用';
                }
                $extra = "";
                // $arg["data"]["DETERMINED_COURSE"] = knjCreateTextBox($objForm, $Row["DETERMINED_COURSE"], "DETERMINED_COURSE", 80, 80, $extra);
                $arg["data"]["DETERMINED_COURSE"] = getTextOrArea($objForm, "DETERMINED_COURSE", 40, 1, $Row["DETERMINED_COURSE"]);
                $arg["data"]["DETERMINED_COURSE_SIZE"] = '<font size="2" color="red">(全角40文字まで)</font>';
                //内容
                $extra = "style=\"height:60px; overflow:auto;\"";
                // $arg["data"]["COURSE_CONTENTS"] = knjCreateTextArea($objForm, "COURSE_CONTENTS", 4, 71, "soft", $extra, $Row["COURSE_CONTENTS"]);
                $arg["data"]["COURSE_CONTENTS"] = getTextOrArea($objForm, "COURSE_CONTENTS", 35, 4, $Row["COURSE_CONTENTS"]);
                $arg["data"]["COURSE_CONTENTS_SIZE"] = '<font size="2" color="red">(全角35文字X4行まで)</font>';

                //その他 (固定 RECORD_NO='3' RECORD_SEQ=1のみ)
                if (isset($model->schregno) && !isset($model->warning)) {
                    $Row2 = $db->getRow(knje390mQuery::getSubQuery4CareerguidanceRecordList($model, "6"), DB_FETCHMODE_ASSOC);
                } else {
                    $Row2 =& $model->field4;
                }
                $extra = "style=\"height:75px; overflow:auto;\"";
                // $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 5, 81, "soft", $extra, $Row2["REMARK"]);
                $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", 40, 5, $Row["REMARK"]);
                $arg["data"]["REMARK_SIZE"] = '<font size="2" color="red">(全角40文字X5行まで)</font>';

            //2:校内実習、3:職場見学、4:職場実習
            } else {
                //ヘッダー
                if ($model->field4["RECORD_DIV"] === '2') {
                    $arg["data"]["RECORD_NAME"] = '校内実習';
                } elseif ($model->field4["RECORD_DIV"] === '3') {
                    $arg["data"]["RECORD_NAME"] = '職場見学';
                } else {
                    $arg["data"]["RECORD_NAME"] = '職場実習';
                }
                $arg["RECORD_DIV234"] = "1";
                //期日開始
                $Row["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $Row["WORK_TRAINING_S_DATE"]);
                $arg["data"]["WORK_TRAINING_S_DATE"] = View::popUpCalendar($objForm, "WORK_TRAINING_S_DATE", $Row["WORK_TRAINING_S_DATE"]);
                //期日終了日
                if ($model->field4["RECORD_DIV"] === '2' || $model->field4["RECORD_DIV"] === '4') {
                    $arg["E_DATE"] = "1";
                    $Row["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $Row["WORK_TRAINING_E_DATE"]);
                    $arg["data"]["WORK_TRAINING_E_DATE"] = View::popUpCalendar($objForm, "WORK_TRAINING_E_DATE", $Row["WORK_TRAINING_E_DATE"]);
                }
                //期日終了日
                if ($model->field4["RECORD_DIV"] === '3' || $model->field4["RECORD_DIV"] === '4') {
                    $arg["PLACE"] = "1";
                    //場所
                    $extra = "";
                    // $arg["data"]["WORK_TRAINING_PLACE"] = knjCreateTextBox($objForm, $Row["WORK_TRAINING_PLACE"], "WORK_TRAINING_PLACE", 40, 40, $extra);
                    $arg["data"]["WORK_TRAINING_PLACE"] = getTextOrArea($objForm, "WORK_TRAINING_PLACE", 20, 1, $Row["WORK_TRAINING_PLACE"]);
                    $arg["data"]["WORK_TRAINING_PLACE_SIZE"] = '<font size="2" color="red">(全角20文字まで)</font>';
                }
                //支援・手立てのサイズ
                $suportSize = "71";
                $suportSize_html = "35";
                if ($model->field4["RECORD_DIV"] === '3') {
                    $arg["SUPPORT_TARGET"] = "1";
                    //支援・手立て(グループ・個人)
                    $suportSize = "63";
                    $suportSize_html = "31";
                    $extra = "style=\"height:75px; overflow:auto;\"";
                    // $arg["data"]["WORK_TRAINING_SUPPORT_TARGET"] = knjCreateTextArea($objForm, "WORK_TRAINING_SUPPORT_TARGET", 5, 13, "soft", $extra, $Row["WORK_TRAINING_SUPPORT_TARGET"]);
                    $arg["data"]["WORK_TRAINING_SUPPORT_TARGET"] = getTextOrArea($objForm, "WORK_TRAINING_SUPPORT_TARGET", 6, 5, $Row["WORK_TRAINING_SUPPORT_TARGET"]);
                    $arg["data"]["WORK_TRAINING_SUPPORT_TARGET_SIZE"] = '<font size="2" color="red">(全角6文字5行まで)</font>';
                }
                
                //内容
                $extra = "";
                // $arg["data"]["WORK_TRAINING_CONTENTS"] = knjCreateTextBox($objForm, $Row["WORK_TRAINING_CONTENTS"], "WORK_TRAINING_CONTENTS", 80, 80, $extra);
                $arg["data"]["WORK_TRAINING_CONTENTS"] = getTextOrArea($objForm, "WORK_TRAINING_CONTENTS", 40, 1, $Row["WORK_TRAINING_CONTENTS"]);
                $arg["data"]["WORK_TRAINING_CONTENTS_SIZE"] = '<font size="2" color="red">(全角40文字まで)</font>';
                //目標
                $extra = "style=\"height:75px; overflow:auto;\"";
                // $arg["data"]["WORK_TRAINING_GOAL"] = knjCreateTextArea($objForm, "WORK_TRAINING_GOAL", 5, 71, "soft", $extra, $Row["WORK_TRAINING_GOAL"]);
                $arg["data"]["WORK_TRAINING_GOAL"] = getTextOrArea($objForm, "WORK_TRAINING_GOAL", 35, 5, $Row["WORK_TRAINING_GOAL"]);
                //支援・手立て
                // $arg["data"]["WORK_TRAINING_SUPPORT"] = knjCreateTextArea($objForm, "WORK_TRAINING_SUPPORT", 5, $suportSize, "soft", $extra, $Row["WORK_TRAINING_SUPPORT"]);
                $arg["data"]["WORK_TRAINING_SUPPORT"] = getTextOrArea($objForm, "WORK_TRAINING_SUPPORT", $suportSize_html, 5, $Row["WORK_TRAINING_SUPPORT"]);
                //成果
                // $arg["data"]["WORK_TRAINING_RESULT"] = knjCreateTextArea($objForm, "WORK_TRAINING_RESULT", 5, 71, "soft", $extra, $Row["WORK_TRAINING_RESULT"]);
                $arg["data"]["WORK_TRAINING_RESULT"] = getTextOrArea($objForm, "WORK_TRAINING_RESULT", 35, 5, $Row["WORK_TRAINING_RESULT"]);
                //課題
                // $arg["data"]["WORK_TRAINING_CHALLENGE"] = knjCreateTextArea($objForm, "WORK_TRAINING_CHALLENGE", 5, 71, "soft", $extra, $Row["WORK_TRAINING_CHALLENGE"]);
                $arg["data"]["WORK_TRAINING_CHALLENGE"] = getTextOrArea($objForm, "WORK_TRAINING_CHALLENGE", 35, 5, $Row["WORK_TRAINING_CHALLENGE"]);
                $arg["data"]["WORK_TRAINING_SIZE"] = '<font size="2" color="red">(全角35文字X5行まで)</font>';
                $arg["data"]["WORK_TRAINING_SUPPORT_SIZE"] = '<font size="2" color="red">(全角'.$suportSize_html.'文字X5行まで)</font>';
            }
            //5:年間の成果、課題
        } else {
            $arg["RECORD_DIV5"] = "1";
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row3 = $db->getRow(knje390mQuery::getSubQuery4CareerguidanceResultChallengeGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row3 =& $model->field4;
            }
            //年間の成果
            $extra = "style=\"height:75px; overflow:auto;\"";
            // $arg["data"]["CAREER_GUIDANCE_RESULT"] = knjCreateTextArea($objForm, "CAREER_GUIDANCE_RESULT", 5, 71, "soft", $extra, $Row3["CAREER_GUIDANCE_RESULT"]);
            $arg["data"]["CAREER_GUIDANCE_RESULT"] = getTextOrArea($objForm, "CAREER_GUIDANCE_RESULT", 35, 5, $Row3["CAREER_GUIDANCE_RESULT"]);
            //年間の課題
            // $arg["data"]["CAREER_GUIDANCE_CHALLENGE"] = knjCreateTextArea($objForm, "CAREER_GUIDANCE_CHALLENGE", 5, 71, "soft", $extra, $Row3["CAREER_GUIDANCE_CHALLENGE"]);
            $arg["data"]["CAREER_GUIDANCE_CHALLENGE"] = getTextOrArea($objForm, "CAREER_GUIDANCE_CHALLENGE", 35, 5, $Row3["CAREER_GUIDANCE_CHALLENGE"]);
            $arg["data"]["CAREER_GUIDANCE_SIZE"] = '<font size="2" color="red">(全角35文字X5行まで)</font>';
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
        View::toHTML($model, "knje390mSubForm4_4.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    $retCnt = 0;
    $no1Cnt = 1;
    $no2Cnt = 1;
    $query = knje390mQuery::getSubQuery4CareerguidanceRecordList($model, "");
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field4["RECORD_DIV"] === '1') {
            $rowlist["CONTENTS_NAME"] = '相談懇談'.($retCnt+1);
            $rowlist["MEETING_DATE"] = str_replace("-", "/", $rowlist["MEETING_DATE"]);
            $rowlist["CONTENTS_NAIYOU"] = '会議日:'.$rowlist["MEETING_DATE"].'　概要:'.substr($rowlist["MEETING_SUMMARY"], 0, 120);
        } elseif ($model->field4["RECORD_DIV"] === '2') {
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_E_DATE"]);
            $rowlist["CONTENTS_NAME"] = '校内実習'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'～'.$rowlist["WORK_TRAINING_E_DATE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } elseif ($model->field4["RECORD_DIV"] === '3') {
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["CONTENTS_NAME"] = '職場見学'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'　場所:'.$rowlist["WORK_TRAINING_PLACE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } elseif ($model->field4["RECORD_DIV"] === '4') {
            $rowlist["WORK_TRAINING_S_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_S_DATE"]);
            $rowlist["WORK_TRAINING_E_DATE"] = str_replace("-", "/", $rowlist["WORK_TRAINING_E_DATE"]);
            $rowlist["CONTENTS_NAME"] = '職場実習'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '期日:'.$rowlist["WORK_TRAINING_S_DATE"].'～'.$rowlist["WORK_TRAINING_E_DATE"].'　場所:'.$rowlist["WORK_TRAINING_PLACE"].'　内容:'.$rowlist["WORK_TRAINING_CONTENTS"];
        } elseif ($model->field4["RECORD_DIV"] === '6') {
            if ($rowlist["RECORD_NO"] === '1') {
                $rowlist["CONTENTS_NAME"] = '事業所'.$no1Cnt;
                $no1Cnt++;
            } else {
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //構成員参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=team_member_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "構成員参照", $extra.$disabled);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('careerguidance4_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('careerguidance4_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('careerguidance4_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    
    //5:年間の成果・課題の更新、6:決定進路内容のその他
    //更新ボタン
    $extra = "onclick=\"return btn_submit('careerguidance4_update2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform4A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

