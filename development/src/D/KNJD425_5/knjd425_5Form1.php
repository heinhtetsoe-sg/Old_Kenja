<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425_5Form1
{
    function main(&$model)
    {
        $objForm = new form;

        // Add by HPA textarea_cursor 2020-02-03 start
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJD425_5Form1_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJD425_5Form1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJD425_5Form1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for textarea_cursor 2020/02/20 end

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform5", "POST", "knjd425_5index.php", "", "subform5");

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //更新日
        //更新日(プロパティが立っているときのみ表示)
        if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
            $arg["data"]["UPDDATE"] = "更新日:".$model->upddate;
        }

        //所感タイトル
        $db = Query::dbCheckOut();
        //$query = knjd425_5Query::getGuidanceKindName($model, $model->selKindNo);
        //$result = $db->query($query);
        $result = $this->loadGuidanceKindName($db, $model, $model->selKindNo);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $arg["data"]["TITLE"] = $row["BTN_SUBFORMCALL"];
        }

        $db = Query::dbCheckOut();

        // Add by HPA for title start 2020/02/03
        $nameShow = $arg["data"]["NAME_SHOW"];
        $data_title = $arg["data"]["TITLE"];
        $htmlTitle = "".$nameShow."の".$data_title."の情報画面";
        echo "<script>
        var title= '".$htmlTitle."';
        </script>";
        // Add by HPA for title end 2020/02/20

        //評価項目リスト作成
        $evalItemList = array();
        $colcnt = 0;
        if ($model->selKindNo == "8") {
            $titlelist = array();
            $titlelist["NAME"] = "教科・領域";
            $arg["headlist"][] = $titlelist;
            $colcnt++;
            $query = knjd425_5Query::getEvalView($model);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                for ($ib = 1;$ib <= 8;$ib++) {
                    if ($row["ITEM_REMARK".$ib] != "") {
                        $evalItemList[] = $row["ITEM_REMARK".$ib];
                    }
                }
            }
        } else {
            $evalItemList[] = "評価";
            $titlelist = array();
            $titlelist["NAME"] = "教科";
            $arg["headlist"][] = $titlelist;
            $colcnt++;
        }
        for ($iw = 0; $iw < get_count($evalItemList);$iw++) {
            $titlelist["NAME"] = $evalItemList[$iw];
            $arg["headlist"][] = $titlelist;
            $colcnt++;
        }

        $arg["data"]["MERGECEL"] = $colcnt;

        //科目数を取得する。
        $query = knjd425_5Query::getEvalSubject($model, true);
        $sbjcnt = $db->getOne($query);

        //データ取得
        if (get_count($model->remarkarry) < 1) {
            //未入力なら、DBから取得。
            $recarry = array();
            $recarry[] = $this->getDataList($db, $model, get_count($evalItemList)*$sbjcnt);
        } else {
            //入力中なら、ここ。
            $addwk = array();
            for ($dcnt = 0;$dcnt < get_count($model->remarkarry);$dcnt++) {
                $addwk["REMARK".($dcnt+1)] = $model->remarkarry[$dcnt];
                if ($sbjcnt == 1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                } else {
                    if ($dcnt % $sbjcnt == $sbjcnt-1) {
                        $recarry[] = $addwk;
                        $addwk = array();
                    }
                }
            }
            //抜けたタイミングで$addwkの個数が追加タイミングの場合をカバー
            if (get_count($addwk) > 0 && get_count($model->remarkarry) != 1 && ($dcnt % get_count($evalItemList) == get_count($evalItemList) - 1)) {
                $recarry[] = $addwk;
                $addwk = array();
            }
        }

        //科目リスト
        $query = knjd425_5Query::getEvalSubject($model);
        $result = $db->query($query);
        $tr_td = "";
        $nx = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SUBCLASSNAME"] !== null && $row["SUBCLASSNAME"] !== "") {
              /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
                $tr_td .= "<tr class=\"no_search\"><th aria-label =\"\">";
                $tr_td .= $row["SUBCLASSNAME"];
                $tr_td .= "</th>";
                for ($iv = 0; $iv < get_count($evalItemList);$iv++) {
                    $tr_td .= "<td bgcolor=\"#ffffff\">";
                    $val = "";
                    $extra = "";
                    $wkarry = $recarry[$iv];
                    $extra = "id=\"REMARK_".$nx."_".$iv."\" aria-label= \"".$row["SUBCLASSNAME"]." 全角38文字X25行まで\"";
                    $val = $wkarry["REMARK".( ($nx*($iv+1)) + 1 )];
                    $tr_td .= knjCreateTextArea($objForm, "REMARK_".$nx."_".$iv, 10, 76, "", $extra, $val);
                    $tr_td .= "<BR><font size=2, color=\"red\">(全角38文字X25行まで)</font><span id=\"statusarea".($nx*($iv+1))."\" style=\"color:blue\"></span>";
                    /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
                    knjCreateHidden($objForm, "REMARK_".$nx."_".$iv."_KETA", 76);
                    knjCreateHidden($objForm, "REMARK_".$nx."_".$iv."_GYO", 25);
                    KnjCreateHidden($objForm, "REMARK_".$nx."_".$iv."_STAT", "statusarea".($nx*($iv+1)));
                    $tr_td .= "</td>";
                }
                $tr_td .= "</tr>";
                $nx++;
            }
        }

        $arg["list"][] = array("tr_td" => $tr_td);
        Query::dbCheckIn($db);

        //戻るボタンを作成する
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        /* Edit by HPA for current_cursor start 2020/02/03 */
        $extra = "id = \"update5\" onclick=\"current_cursor('update5');return btn_submit('update5');\"";
        /* Edit by HPA for current_cursor end 2020/02/20 */
        $arg["btn_update5"] = KnjCreateBtn($objForm, "btn_update5", "更新", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", get_count($evalItemList));
        knjCreateHidden($objForm, "HID_ROWCNT", $nx);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425_5Form1.html", $arg);
    }

    function getDataList($db, $model, $outcnt) {
        $retlist = array();
        $query = knjd425_5Query::chkDataExist($model);
        $chkcnt = $db->getOne($query);
        if ($chkcnt > 0) {
            for ($ii = 1;$ii <= $outcnt;$ii++) {
                $query = knjd425_5Query::getDataList($model, $ii);
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
        $query = knjd425_5Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は学籍番号抜きで取り直し。
            $specifyschregflg = "1";
            $query = knjd425_5Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
            if (get_count($result) > 0) {
                //if文を抜けて最後の処理を実施。
            } else {
                //データが無い時は年組、学籍番号抜きで取り直し。
                $specifyschregflg = "2";
            }
        }

        $query = knjd425_5Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}
?>
