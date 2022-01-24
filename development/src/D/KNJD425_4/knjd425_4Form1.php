<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425_4Form1
{
    function main(&$model)
    {
        $objForm = new form;

        // Add by HPA textarea_cursor 2020-02-03 start
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJD425_4Form1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJD425_4Form1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJD425_4Form1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020/02/20 end

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform4", "POST", "knjd425_4index.php", "", "subform4");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //更新日
        //更新日(プロパティが立っているときのみ表示)
        if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
            $arg["data"]["UPDDATE"] = "更新日:".$model->upddate;
        }

        //所感タイトル
        $db = Query::dbCheckOut();
        //$query = knjd425_4Query::getGuidanceKindName($model, $model->selKindNo);
        //$result = $db->query($query);
        $result = $this->loadGuidanceKindName($db, $model, $model->selKindNo);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["TITLE"] = $row["BTN_SUBFORMCALL"];
        }

        // Add by HPA for title start 2020/02/03
        $nameShow = $arg["data"]["NAME_SHOW"];
        $data_title = $arg["data"]["TITLE"];
        $htmlTitle = "".$nameShow."の".$data_title."の情報画面";
        echo "<script>
        var title= '".$htmlTitle."';
        </script>";
        // Add by HPA for title end 2020/02/20

        //入力項目件数を取得
        $query = knjd425_4Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $setbuf = array();
            if ($row["KIND_REMARK"] !== null && $row["KIND_REMARK"] !== "") {
                $outcnt++;
            }
        }
        //outcnt最大チェック
        $outcnt = $outcnt > 3 ? 3 : $outcnt;
        $arg["data"]["MERGECEL"] = $outcnt;

        //データ取得
        if (get_count($model->remarkarry) < 1) {
            //未入力なら、DBから取得。
            $recarry = array();
            $recarry[] = $this->getDataList($db, $model, $outcnt);
        } else {
            //入力中なら、ここ。
            $addwk = array();
            for ($dcnt = 0;$dcnt < $outcnt;$dcnt++) {
                $addwk["REMARK".($dcnt % $outcnt+1)] = $model->remarkarry[$dcnt];
                if (get_count($model->remarkarry) == 1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                } else {
                    if ($dcnt % $outcnt == $outcnt-1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                    }
                }
            }
            //抜けたタイミングで$addwkの個数が追加タイミングの場合をカバー
            if (get_count($addwk) > 0 && get_count($model->remarkarry) != 1 && $dcnt % $outcnt == $outcnt-1) {
                $recarry[] = $addwk;
                $addwk = array();
            }
        }

        //改めて入力項目を設定
        $mojilenlist = array(15, 15, 10);
        $query = knjd425_4Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $setbuf = array();
            //項目数3以降は無視。
            if ($outcnt < 3) {
                if ($row["KIND_REMARK"] !== null && $row["KIND_REMARK"] !== "") {
                    $setbuf1["REMARKTITLE"] = $row["KIND_REMARK"];
                    $arg["data1"][] = $setbuf1;
                    $wkarry = $recarry[0];
                    $val = $wkarry["REMARK".($outcnt+1)];
                    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                    $extra = "aria-label= \"".$setbuf1["REMARKTITLE"]." 全角".$mojilenlist[$outcnt]."文字X25行まで\" id=\"REMARK_0_".$outcnt."\"";
                    $setbuf2["REMARK"] = knjCreateTextArea($objForm, "REMARK_0_".$outcnt, 10, $mojilenlist[$outcnt]*2, "", $extra, $val);
                    $setbuf2["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角".$mojilenlist[$outcnt]."文字X25行まで)</font><span id=\"statusarea".$outcnt."\" style=\"color:blue\"></span>";
                    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                    knjCreateHidden($objForm, "REMARK_0_".$outcnt."_KETA", $mojilenlist[$outcnt]*2);
                    knjCreateHidden($objForm, "REMARK_0_".$outcnt."_GYO", 25);
                    KnjCreateHidden($objForm, "REMARK_0_".$outcnt."_STAT", "statusarea".$outcnt);
                    $arg["data2"][] = $setbuf2;
                    $outcnt++;
                }
            }
        }
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        /* Edit by HPA for current_cursor start 2020/02/03 */

        $extra = "id = \"update4\" onclick=\"current_cursor('update4');return btn_submit('update4');\"";
        /* Edit by HPA for current_cursor end 2020/02/20 */
        $arg["btn_update4"] = KnjCreateBtn($objForm, "btn_update4", "更新", $extra);

        //hidden
        // $objForm->ae( array("type"      => "hidden",
        //                     "name"      => "cmd" ) );
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425_4Form1.html", $arg);
    }

    function getDataList($db, $model, $outcnt) {
        $retlist = array();
        $query = knjd425_4Query::chkDataExist($model);
        $chkcnt = $db->getOne($query);
        if ($chkcnt > 0) {
            for ($ii = 1;$ii <= $outcnt;$ii++) {
                $query = knjd425_4Query::getDataList($model, $ii);
                $result = $db->query($query);
                $getval = "";
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["REMARK"] !== null && $row["REMARK"] !== "") {
                        $getval = $row["REMARK"];
                    }
                }
                $retlist["REMARK".$ii] = $getval;
            }
        }
        return $retlist;
    }

    //項目名称取得
    function loadGuidanceKindName($db, $model, $kno="")
    {
        //データの優先度として、個人>年組>年度となる。データが無ければ下位SQLでデータを取得していく。
        $specifyschregflg = "";
        $query = knjd425_4Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は学籍番号抜きで取り直し。
            $specifyschregflg = "1";
            $query = knjd425_4Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
            if (get_count($result) > 0) {
                //if文を抜けて最後の処理を実施。
            } else {
                //データが無い時は年組、学籍番号抜きで取り直し。
                $specifyschregflg = "2";
            }
        }

        $query = knjd425_4Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}
?>
