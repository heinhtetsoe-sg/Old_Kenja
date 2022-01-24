<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425_3Form1
{
    function main(&$model)
    {
        $objForm = new form;

        // Add by HPA textarea_cursor 2020-02-03 start
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJD425_3Form1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJD425_3Form1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJD425_3Form1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020/02/20 end

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knjd425_3index.php", "", "subform3");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //更新日(プロパティが立っているときのみ表示)
        if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
            $arg["data"]["UPDDATE"] = "更新日:".$model->upddate;
        }

        //所感タイトル
        $db = Query::dbCheckOut();
        //$query = knjd425_3Query::getGuidanceKindName($model, $model->selKindNo);
        //$result = $db->query($query);
        $result = $this->loadGuidanceKindName($db, $model, $model->selKindNo);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["TITLE"] = $row["BTN_SUBFORMCALL"];
        }
        $result->free();
        // Add by HPA for title start 2020/02/03
        $nameShow = $arg["data"]["NAME_SHOW"];
        $data_title = $arg["data"]["TITLE"];

        $htmlTitle = "".$nameShow."の".$data_title."の情報画面";
        echo "<script>
        var title = '".$htmlTitle."';
        </script>";
         // Add by HPA for title end 2020/02/20

        //入力項目件数を取得
        $query = knjd425_3Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_REMARK"] !== null && $row["KIND_REMARK"] !== "") {
                $outcnt++;
            }
        }
        $result->free();

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
                if ($outcnt == 1) {
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
        $query = knjd425_3Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_REMARK"] !== null && $row["KIND_REMARK"] !== "") {
                $setbuf = array();
                $setbuf["REMARKTITLE"] = $row["KIND_REMARK"];
                $wkarry = $recarry[0];
                /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                $extra = "id=\"REMARK_0_".$outcnt."\" aria-label= \"".$setbuf["REMARKTITLE"]." 全角40文字X25行まで\"";
                $val = $wkarry["REMARK".($outcnt+1)];
                $setbuf["REMARK"] = knjCreateTextArea($objForm, "REMARK_0_".$outcnt, 10, 80, "", $extra, $val);
                $setbuf["EXTFMT"] .= "<BR><font size=2, color=\"red\">(全角40文字X25行まで)<span id=\"statusarea".$outcnt."\" style=\"color:blue\"></span>";
                /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                knjCreateHidden($objForm, "REMARK_0_".$outcnt."_KETA", 80);
                knjCreateHidden($objForm, "REMARK_0_".$outcnt."_GYO", 25);
                KnjCreateHidden($objForm, "REMARK_0_".$outcnt."_STAT", "statusarea".$outcnt);
                $arg["list"][] = $setbuf;
                $outcnt++;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */ 
        $extra = "id = \"update3\" onclick=\"current_cursor('update3');return btn_submit('update3');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
        $arg["btn_update3"] = KnjCreateBtn($objForm, "btn_update3", "更新", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425_3Form1.html", $arg);
    }

    function getDataList($db, $model, $outcnt) {
        $retlist = array();
        $query = knjd425_3Query::chkDataExist($model);
        $chkcnt = $db->getOne($query);
        if ($chkcnt > 0) {
            for ($ii = 1;$ii <= $outcnt;$ii++) {
                $query = knjd425_3Query::getDataList($model, $ii);
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
        $query = knjd425_3Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は学籍番号抜きで取り直し。
            $specifyschregflg = "1";
            $query = knjd425_3Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
            if (get_count($result) > 0) {
                //if文を抜けて最後の処理を実施。
            } else {
                //データが無い時は年組、学籍番号抜きで取り直し。
                $specifyschregflg = "2";
            }
        }

        $query = knjd425_3Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}
?>
