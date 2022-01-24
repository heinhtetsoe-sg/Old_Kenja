<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje100Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $chk_schregno;

    /**
     * メンバ変数の初期化
     */
    function knje100Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("GRADE")){
            $this->field["GRADE"]   = VARS::post("GRADE");    //学年
        }
        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS");
        }
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
            //ファイル取り込み
            $dataArr = array();
            $dataArr = $this->fileUpload();
            $erraDatas = "";
            $header = array("YEAR"              => "年度",
                            "SHAMEXAMCD"        => "試験コード",
                            "SCHREGNO"          => "学籍番号",
                            "CLASSCD"           => "教科コード",
                            "SCHOOL_KIND"       => "学校校種",
                            "CURRICULUM_CD"     => "教育課程コード",
                            "SUBCLASSCD"        => "科目コード"
                        );

            $tables = array("1" => "SHAMEXAMINATION_DAT");
            $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
            }
            if (sizeof($this->errData) > 0) {
                $erraDatas .= "行番号　メッセージ"."\\n";
            }
            foreach ($this->errData as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            if ($erraDatas != "") {
                $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
            } else {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i<get_count($dataArr); $i++) {
                    $query = knje100Query::deleteQueryCsv($dataArr[$i]);   //模試データの削除
                    $result = $db->query($query);
                    $query = knje100Query::insertQueryCsv($dataArr[$i]);   //模試データの作成（ＣＳＶデータより読込）
                    $result = $db->query($query);
                }
                $db->commit(); // トランザクションをコミットする。
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($dataArr)."件");
            }
    }

    function fileUpload()
    {
        $this->errData = array();
        $temp_arr = array();
        $checkdata = array( "YEAR"          => array(4, "年度"),
                            "SHAMEXAMCD"    => array(2, "試験コード"),
                            "SCHREGNO"      => array(8, "学籍番号"),
                            "SUBCLASSCD"    => array(6, "科目"),
                            "SUBCLASSNAME"  => array(21, "科目名"),
                            "SCORE"         => array(4, "得点"),
                            "DEVIATION"     => array(4, "全国偏差値"));
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $chk_schregno = "";

        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i = 0;
            $line = 1;  //行数
            $header = true;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SHAMEXAMINATION_DAT');
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if ($header) {
                        $header = false;
                        $line++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS");

                    $tmp = explode(",", $buffer);
                    $data = array();
                    $data = array("YEAR"            => $tmp[0],
                                  "SHAMEXAMCD"      => $tmp[1],
                                  "SCHREGNO"        => $tmp[2],
                                  "SUBCLASSCD"      => $tmp[3],
                                  "SUBCLASSNAME"    => $tmp[4],
                                  "SCORE"           => (strlen(trim($tmp[5]))) ? number_format($tmp[5], 1) : "",
                                  "DEVIATION"       => (strlen(trim($tmp[6]))) ? number_format($tmp[6], 1) : "");

                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }
    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;
        $masterErrFlg       = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";
        $masterErrMsg       = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $valuesSep          = "";
        $masterSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            if (in_array($key, array("YEAR","SHAMEXAMCD","SUBCLASSCD","SCORE","DEVIATION"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            if (in_array($key, array("SCHREGNO")) && strlen($data[$key]) == 6) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            if (in_array($key, array("SUBCLASSNAME")) && strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //年度チェック
            if (in_array($key, array("YEAR")) && $this->control["年度"] != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正", $valuesSep, $val[1]);
            }
            //模試コードチェック
            if (in_array($key, array("SHAMEXAMCD")) && $this->field["GRADE"] != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正", $valuesSep, $val[1]);
            }
            //存在チェック
            if (in_array($key, array("SCHREGNO")) && $data[$key] != "") {
                $query = knje100Query::checkQuerySch($data);
                if ($db->getOne($query) < 1) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            //科目マスタチェック
            if (in_array($key, array("SUBCLASSCD")) && $data[$key] != "") {
                $query = knje100Query::checkQuerySub($data);
                if ($db->getOne($query) < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
