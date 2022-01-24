<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje060Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $year;
    var $file;
    var $annual;            //年次
    var $control;           //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knje060Model()
    {
        $this->year = CTRL_YEAR;
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        if (VARS::file("FILE")){
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     =>  VARS::post("KIND"),         //種別
                            "RANGE"     =>  VARS::post("RANGE"),        //生成範囲
                            "METHOD"    =>  VARS::post("METHOD"),       //生成方法
                            "REPLACE"   =>  VARS::post("REPLACE")       //評価が１の場合２に置き換える
                            );
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if ($this->field["KIND"] == 2 && $this->field["METHOD"] == 2){
            //ファイル取り込み
            $this->dataArr = array();
            $this->errData = array();
            $this->dataArr = $this->fileUpload();
            //データの整合性チェック
            $erraDatas = "";
            $tables = array("1" => "SCHREG_STUDYREC_DAT", "2" => "STUDYCLASSREMARK_DAT");
            $header = array("SCHOOLCD"          => "本校区分",
                            "YEAR"              => "年度",
                            "SCHREGNO"          => "学籍番号",
                            "ANNUAL"            => "年次",
                            "CLASSCD"           => "教科コード",
                            "SCHOOL_KIND"       => "学校校種",
                            "CURRICULUM_CD"     => "教育課程コード",
                            "SUBCLASSCD"        => "科目コード");
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
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //学籍学習記録データの削除
                    knje060Query::deleteQuerySchreg_StudyRec_Dat($this->dataArr[$i]);

                    //CLASS_MSTより科目数を取得
                    $subclasses = $db->getOne(knje060Query::getSubclasses($this->dataArr[$i]));
                    $this->dataArr[$i]["SUBCLASSES"] = $subclasses;

                    //学籍学習記録データの作成（ＣＳＶデータより読込)
                    knje060Query::addQuerySchreg_StudyRec_Dat($this->dataArr[$i]);
                    knje060Query::deleteStudyclassremark($this->dataArr[$i]);
                    knje060Query::insertStudyclassremark($this->dataArr[$i]);
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }elseif ($this->field["KIND"] == 2 && $this->field["METHOD"] == 1){ //"成績データ抽出中..."
            //成績データの取得
            knje060Query::addRecord_Schreg_Studyrec_Dat($this);
            $this->message = "学籍学習記録データの生成が完了しました。";
        }else if ($this->field["KIND"] == 1){ //"勤怠データ抽出＆集計中..."
            //出欠学期別累積データより集計
            knje060Query::addRecord_Schreg_Attendrec_Dat($this);
            $this->message = "学籍出欠記録データの生成が完了しました。";
        }
    }

    function fileUpload()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (is_uploaded_file ($this->file["tmp_name"])){
            $tempArr = array();
            //学籍学習記録データのサイズ
            $checkdata = array();
            $checkdata = array("SCHOOLCD"         => array(1,"本校区分"),
                                "YEAR"            => array(4,"年度"),
                                "SCHREGNO"        => array(8,"学籍番号"),
                                "ANNUAL"          => array(2,"年次"),
                                "CLASSCD"         => array(2,"教科コード"),
                                "SUBCLASSCD"      => array(6,"科目コード"),
                                "CLASSNAME"       => array(30,"教科名"),
                                "CLASSABBV"       => array(15,"教科略称"),
                                "CLASSNAME_ENG"   => array(40,"教科名称英字"),
                                "CLASSABBV_ENG"   => array(30,"教科略称英字"),
                                "SUBCLASSNAME"    => array(60,"科目名"),
                                "SUBCLASSABBV"    => array(9,"科目略称"),
                                "SUBCLASSNAME_ENG"=> array(40,"科目名称英字"),
                                "SUBCLASSABBV_ENG"=> array(20,"科目略称英字"),
                                "VALUATION"       => array(2,"評定"),
                                "GET_CREDIT"      => array(2,"標準修得単位"),
                                "ADD_CREDIT"      => array(2,"増加修得単位"),
                                "REMARK"          => array(60,"備考"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");

            $i = 0;
            $line = 1;  //行数

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_STUDYREC_DAT');
            $primaryKey2 = $this->getPrimaryKey('STUDYCLASSREMARK_DAT');

            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS"));
                    $tmp = common::csv2array($buffer);
                    $data = array();
                    $data = array("SCHOOLCD"          => $tmp[0],
                                    "YEAR"            => $tmp[8],
                                    "SCHREGNO"        => $tmp[4],
                                    "ANNUAL"          => $tmp[9],
                                    "CLASSCD"         => $tmp[14],
                                    "SUBCLASSCD"      => $tmp[19],
                                    "CLASSNAME"       => $tmp[15],
                                    "CLASSABBV"       => $tmp[16],
                                    "CLASSNAME_ENG"   => $tmp[17],
                                    "CLASSABBV_ENG"   => $tmp[18],
                                    "SUBCLASSNAME"    => $tmp[20],
                                    "SUBCLASSABBV"    => $tmp[21],
                                    "SUBCLASSNAME_ENG"=> $tmp[22],
                                    "SUBCLASSABBV_ENG"=> $tmp[23],
                                    "VALUATION"       => $tmp[24],
                                    "GET_CREDIT"      => $tmp[25],
                                    "ADD_CREDIT"      => $tmp[26],
                                    "REMARK"          => $tmp[27]
                                );
                    $data = str_replace("'", "''", $data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ

        $hissuSep           = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";
        $numericSep         = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SUBCLASSCD", "GET_CREDIT"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if (in_array($key, array("VALUATION", "ADD_CREDIT","GET_CREDIT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
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
//var_dump($_REQUEST);
?>
