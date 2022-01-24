<?php

require_once('for_php7.php');

class knjp737Model extends Model {
    var $field = array();
    var $cmd;

    function knjp737Model() { }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->school_kind);
            unset($this->s_row_no);
            unset($this->row_no_cnt);
            $this->field = array();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }
        if (VARS::post("SCHOOL_KIND") != "") {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }
        if (VARS::post("S_ROW_NO") != "") {
            $this->s_row_no = VARS::post("S_ROW_NO");
        }
        if (VARS::post("ROW_NO_CNT") != "") {
            $this->row_no_cnt = VARS::post("ROW_NO_CNT");
        }

        if ($this->row_no_cnt > 0 && $this->s_row_no > 0) {
            $this->field = array();
            for ($i = $this->s_row_no; $i < ($this->row_no_cnt + $this->s_row_no); $i++) {
                $this->field["GRP_CD_".$i] =  VARS::post("GRP_CD_".$i);
            }
        } else {
            $this->field = array();
        }

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->dataArr = array();
        $this->errData = array();
        $this->dataArr = $this->GetFileString();
        $erraDatas = "";
        if (sizeof($this->errData) > 0) {
            $erraDatas .= "行番号　メッセージ"."\\n";
        }
        foreach ($this->errData as $errKey => $errVal) {
            $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
        }
        if ($erraDatas != "") {
            $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$erraDatas);
        } else {
            knjp737Query::csvHeadDelIns($this);
            $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString()
    {
        $tempArr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                        $this->errCheck($db, $tmp[$t], $line);
                    }
                    $tempArr = $tmp;
                }
                break;
            }
            fclose($fp);
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $line)
    {
        $returnFlg    = true;
        $lengthErrFlg = false;    //レングスエラーフラグ
        $lengthErrMsg = "";       //レングスエラーメッセージ
        $lengthSep    = "";

        if (strlen($data) > 60) {
            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "ヘッド名称（".$data."）");
        }

        if (!$returnFlg) {
            $checkError = $lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }

    //入力項目チェック
    function validateInput() {

        if ($this->cmd == "update") {
            //必須チェック
            if ($this->year == "") {
                $this->setWarning("MSG304", "　　　（ 年度 ）");
                return false;
            }
            if ($this->school_kind == "") {
                $this->setWarning("MSG304", "　　　（ 校種 ）");
                return false;
            }
            if ($this->s_row_no == "") {
                $this->setWarning("MSG304", "　　　（ 開始列番号 ）");
                return false;
            }
            if ($this->row_no_cnt == "") {
                $this->setWarning("MSG304", "　　　（ 項目数 ）");
                return false;
            }
            if ($this->row_no_cnt > 0 && $this->s_row_no > 0) {
                $rownoArray = array();
                for ($i = $this->s_row_no; $i < ($this->row_no_cnt + $this->s_row_no); $i++) {
                    if (!$this->field["GRP_CD_".$i]) {
                        continue;
                    }
                    if ($rownoArray[$this->field["GRP_CD_".$i]] == "1") {
                        $this->setWarning("MSG901", "グループが重複しています。");
                        return false;
                    }
                    $rownoArray[$this->field["GRP_CD_".$i]] = 1;
                }
            }
        } else if ($this->cmd == "delete") {
            //必須チェック
            if ($this->year == "") {
                $this->setWarning("MSG304", "　　　（ 年度 ）");
                return false;
            }
            if ($this->school_kind == "") {
                $this->setWarning("MSG304", "　　　（ 校種 ）");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjp737Query::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->setCmd("main");
        } else {
            $this->setCmd("check");
        }
        return;
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjp737Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp737Query::CheckData($this, "COLLECT_CSV_HEAD_CAPTURE_DAT", $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        $t_year = knjp737Query::CheckData($this, "COLLECT_CSV_INFO_DAT", $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp737Query::CheckData($this, "COLLECT_CSV_HEAD_CAPTURE_DAT", ($this->year - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        $pre_year = knjp737Query::CheckData($this, "COLLECT_CSV_INFO_DAT", ($this->year - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp737Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

}
?>
