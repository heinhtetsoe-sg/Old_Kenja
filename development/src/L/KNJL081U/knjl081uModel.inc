<?php

require_once('for_php7.php');

class knjl081uModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;

    //メンバ変数の初期化
    function knjl081uModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->selectdata);
            unset($this->selectdata2);
        }

        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //辞退者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //手続者一覧
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->testdiv              = VARS::post("TESTDIV");

        if (VARS::file("csvfile")) {
            $this->file = VARS::file("csvfile");      //ファイルのアップロード
            $this->file["name"] = mb_convert_encoding($this->file["name"], "SJIS-win", "UTF-8");
        }
        $this->header        = VARS::post("chk_header");
        $this->csv_radio     = VARS::post("csv");
    }

    function validateInput() {
        //必須チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if (strlen($this->testdiv) == 0) {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    function &update() {
        knjl081uQuery::getUpdateQuery($this);
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
       ☆入試年度(4桁)、☆入試制度(1桁)、☆入試区分(1桁）、受験番号(5桁0埋め)、氏名
    ****************************************************************************************************/
    function CSVUproad() {
        $db = Query::dbCheckOut();

        $valids = array();
        //左と右のリストに存在する受験番号のみを取り込むために取得
        $result = $db->query(knjl081uQuery::getLeftRightList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $valids[] = $row["EXAMNO"];
        }
        $valids = array_unique($valids);
        Query::dbCheckIn($db);

        //続けて更新するので以下の変数を初期化する
        $this->selectdata    = array();   //CSVで取り込んだ受験番号
        $this->selectdata2   = array();   //CSVで取り込んだ以外の受験番号

        if (!is_uploaded_file($this->file["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen ($this->file["tmp_name"], "r");

            $i = $j = 0;
            $err = array();
            $header = false;
            
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != ''){

                    //ヘッダ有りの場合は1行目を無視
                    if ($this->header == "1" && !$header) {
                        $header = true;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $validate = true;

                    //受験番号
                    if (strlen($tmp[5]) < 5) {
                        $tmp[5] = sprintf("%05d", $tmp[5]);
                    }
                    //画面上のキーと一致してない場合は取り込まない
                    if ($tmp[0] != $this->ObjYear | $tmp[1] != $this->applicantdiv | $tmp[3] != $this->testdiv) {
                        $validate = false;
                    }
                    //手続者のみ取り込む
                    if (!in_array($tmp[5], $valids)) {
                        $validate = false;
                    }

                    if ($validate){
                        $this->selectdata[] = str_replace("'", "''", $tmp[5]);
                        $i++;
                    } else {
                        $err[] = $j+1;
                    }
                    $j++;
                }
            }
            fclose($fp);

            if ($i > 0){
            
                //CSVから取り込んだ受験番号以外の合格している受験番号をセット
                $this->selectdata2 = array_diff($valids, $this->selectdata);
                $this->selectdata = implode(",",$this->selectdata);
                $this->selectdata2 = implode(",",$this->selectdata2);
                
                knjl081uQuery::getUpdateQuery($this);
                $this->setMessage("MSG202");
                
                if (get_count($err) > 0){
                    $this->message .= "失敗した行：" .implode($err, ",");
                }
            } else {
                $this->message = "CSV登録に失敗しました。";
            }
        }
    }

    //CSV書き出し
    function CSVDownload() {
        $db = Query::dbCheckOut();
        $data = $head = array();

        //名称取得
        //入試制度
        $query = knjl081uQuery::getNameMst($this->ObjYear, "L003", $this->applicantdiv);
        $infoL003 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //入試区分
        $query = knjl081uQuery::getNameMst($this->ObjYear, "L004", $this->testdiv);
        $infoL004 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //ヘッダ
        $head = array("入試年度","入試制度コード","入試制度","入試区分コード","入試区分","受験番号","氏名");

        //対象者取得
        $result = $db->query(knjl081uQuery::getLeftRightList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["LEFT_FLG"] !== '1') continue; //左リストのみ出力
            $data[] = array( $this->ObjYear                    //0.入試年度
                            ,$this->applicantdiv               //1.入試制度コード
                            ,$infoL003["NAME1"]                //2.入試制度
                            ,$this->testdiv                    //3.入試区分コード
                            ,$infoL004["NAME1"]                //4.入試区分
                            ,$row["EXAMNO"]                    //5.受験番号
                            ,$row["NAME"]);                    //6.氏名
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if (get_count($data) > 0) {
            $contents = "";
            if ($this->header == "1") {
                $contents = implode($head, ",") ."\r\n";
            }

            foreach($data as $csv) {
                $sp = "";
                for ($i = 0; $i < get_count($csv); $i++) {
                    if (strpos($csv[$i], ",")) {
                        $contents .= $sp ."\"" .str_replace('"', '""', $csv[$i]) ."\"";
                    } else {
                        $contents .= $sp .str_replace('"', '""', $csv[$i]);
                    }
                    $sp = ",";
                }
                $contents .= "\r\n";
            }

            $filename = $this->ObjYear."_".$infoL003["NAME1"]."_".$infoL004["NAME1"]."_辞退者一覧".".csv";
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG304");
        }
    }
}
?>
