<?php

require_once('for_php7.php');

//定数定義

class knjl072tModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl072tModel() {}
    function init() {
        $this->cmd           = VARS::request("cmd");
        $this->ObjYear       = CTRL_YEAR + 1;
        $this->selectdata    = VARS::post("selectdata");    //合格者一覧
        $this->selectdata2   = VARS::post("selectdata2");   //候補者一覧
        $this->majorcd       = VARS::post("MAJORCD");   //学科
        $this->wishno        = VARS::post("WISHNO");    //希望順位
        $this->supplement    = VARS::post("SUPPLEMENT");    //追検査者のみ表示する
        $this->applicantdiv  = VARS::post("APPLICANTDIV");
        $this->file          = VARS::file("csvfile");
        $this->header        = VARS::post("chk_header");
        $this->csv_radio     = VARS::post("csv");
    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    function &update() {
        knjl072tQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }

    function CSVModel() {
        if ($this->csv_radio == "1") {
            $this->CSVUproad();
        } elseif ($this->csv_radio == "2") {
            $this->CSVDownload();
        }
    }

    //CSV取り込み、更新
    /************* CSVフォーマット（☆印がキー）********************************************************
       ☆入試年度(4桁)、☆入試制度(1桁)、☆対象者区分(1桁）、受検番号(5桁0埋め)、氏名
    ****************************************************************************************************/
    function CSVUproad() {
        $db = Query::dbCheckOut();

        $valids = array();
        //左と右のリストに存在する受検番号のみを取り込むために取得
        $result = $db->query(knjl072tQuery::GetRightList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $valids[] = $row["EXAMNO"];
        }
        $result = $db->query(knjl072tQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $valids[] = $row["EXAMNO"];
        }
        $valids = array_unique($valids);
        Query::dbCheckIn($db);

        //続けて更新するので以下の変数を初期化する
        $this->selectdata    = array();   //CSVで取り込んだ受検番号
        $this->selectdata2   = array();   //CSVで取り込んだ以外の受検番号

        if (is_uploaded_file ($this->file["tmp_name"])) {
            $fp = fopen ($this->file["tmp_name"], "r");

            $i = $j = 0;
            $err = array();
            $header = false;

            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != '') {

                    //ヘッダ有りの場合は1行目を無視
                    if ($this->header == "1" && !$header) {
                        $header = true;
                        continue;
                    }

                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $validate = true;

                    //受検番号
                    $tmp[6] = sprintf("%05d", $tmp[6]);

                    //画面上のキーと一致してない場合は取り込まない
                    if ($tmp[0] != $this->ObjYear | $tmp[1] != $this->applicantdiv | $tmp[3] != $this->majorcd | $tmp[5] != $this->wishno) {
                        $validate = false;
                    }

                    //候補者のみ取り込む
                    if (!in_array($tmp[6], $valids)) {
                        $validate = false;
                    }

                    if ($validate) {
                        $this->selectdata[] = str_replace("'", "''", $tmp[6]);
                        $i++;
                    }else{
                        $err[] = $j+1;
                    }
                    $j++;
                }
            }
            fclose($fp);

            if ($i > 0) {

                //CSVから取り込んだ受検番号以外の合格している受検番号をセット
                $this->selectdata2 = array_diff($valids, $this->selectdata);
                $this->selectdata = implode(",",$this->selectdata);
                $this->selectdata2 = implode(",",$this->selectdata2);

                knjl072tQuery::getUpdateQuery($this);
                $this->setMessage("MSG202");

                if (get_count($err) > 0) {
                    $this->message .= "失敗した行：" .implode($err, ",");
                }
            } else {
                $this->message = "CSV登録に失敗しました。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }

    //CSV書き出し
    function CSVDownload() {
        $db = Query::dbCheckOut();
        $data = $head = array();

        //名称取得
        $result = $db->query(knjl072tQuery::GetName("L003",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMECD2"] == $this->applicantdiv) $applicantname = $row["NAME1"];
        }

        $major_l_r_name = $db->getOne(knjl072tQuery::getMajorname($this));

        //ヘッダ
        $head = array("入試年度","入試制度コード","入試制度","学科コード","学科","希望順位","受検番号","氏名");

        //対象者取得
        $result = $db->query(knjl072tQuery::GetLeftList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data[] = array( $this->ObjYear                    //入試年度
                            ,$this->applicantdiv               //入試制度
                            ,$applicantname                    //入試制度名称
                            ,$this->majorcd                    //学科コード
                            ,$major_l_r_name                   //学科
                            ,$this->wishno                     //希望順位
                            ,(int) $row["EXAMNO"]              //受検番号
                            ,$row["NAME"]);                    //氏名
        }

        if (get_count($data)>0) {
            $contents = "";
            if ($this->header == "1") {
                $contents = implode($head, ",") ."\r\n";
            }

            foreach($data as $csv) {
                $sp = "";
                for($i = 0; $i < get_count($csv); $i++) {
                    if (strpos($csv[$i], ",")) {
                        $contents .= $sp ."\"" .str_replace('"', '""', $csv[$i]) ."\"";
                    }else{
                        $contents .= $sp .str_replace('"', '""', $csv[$i]);
                    }
                    $sp = ",";
                }
                $contents .= "\r\n";
            }

            $filename = $this->ObjYear.$applicantname."_".$major_l_r_name ."_". $this->wishno .".csv";
            $filename = mb_convert_encoding($filename, "SJIS-win", mb_internal_encoding());
            $contents = mb_convert_encoding($contents, "SJIS-win", mb_internal_encoding());

            /* HTTPヘッダの出力 */
            mb_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            Query::dbCheckIn($db);
            exit;

        } else {
            $this->setWarning("MSG304");
        }
    }
}
?>