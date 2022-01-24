<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjz300Model extends Model
{
    public $field = array();
    public $cmd;
    public $invalid_flag;
    public $year;
    public $csvYear;
    public $userscd;           //primary
    public $staffcd;
    public $sec_competence;
    public $sort;              //昇順,降順
    public $tmp;
    public $userscd2;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化

    public function knjz300Model()
    {

        //ソート処理の初期化
        $this->sort["SRT_U"] = 1;
        $this->sort["SRT_S"] = "";
    }

    public function init()
    {
        $this->file = VARS::file("FILE");

        //無効フラグ 0:オン、1:オフ
        $invalid_flag = (VARS::post("INVALID_FLG") == "") ? "0" : "1";
        $pwdtermchk_flg = (VARS::post("PWDTERMCHK_FLG") == "") ? "0" : "1";
        $chgPwdFlg = (VARS::post("CHG_PWD_FLG") == "") ? "0" : "1";
        $this->field = array("USERSCD"          =>  VARS::post("USERSCD"),     //職員コード
                             "STAFFNAME"        =>  VARS::post("STAFFNAME"),   //職員名
                             "STAFFKANA"        =>  VARS::post("STAFFKANA"),   //職員カナ
                             "USERID"           =>  VARS::post("USERID"),      //ユーザーID
                             "PASSWD"           =>  VARS::post("PASSWD"),      //パスワード
                             "INVALID_FLG"      =>  $invalid_flag,             //無効フラグ
                             "PWDTERMCHK_FLG"   =>  $pwdtermchk_flg,           //パスワード期限
                             "CHG_PWD_FLG"      =>  $chgPwdFlg,                //次回パスワード強制変更
                             "UPDATED"          =>  VARS::post("UPDATED"),     //更新日付
                             "HEADER"           =>  VARS::post("HEADER"),      //ヘッダの有無
                             "OUTPUT"           =>  VARS::post("OUTPUT")       //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        if (VARS::request("USERSCD")) {
            $this->userscd2 = $this->field["USERSCD"];
        }

        $this->userscd = VARS::request("USERSCD");
        $this->cmd     = VARS::request("cmd");

        $tmp = (VARS::request("year"));
        if ($tmp) {
            $this->year = VARS::request("year");
        } else {
            $tmp = (VARS::get("YEAR"));
            if ($tmp) {
                $this->year = VARS::get("YEAR");
            } else {
                $this->year = CTRL_YEAR;
            }
        }
        if (!$this->cmd == "downloadCsv") {
            $this->csvYear = CTRL_YEAR;
        }
        if (VARS::get("csvYear")) {
            $this->csvYear = VARS::get("csvYear");
        }
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD, "knjz300");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //次回パスワード強制変更フィールドの有無
        $db = Query::dbCheckOut();
        $query = knjz300Query::getChgPwdField();
        $this->isChgPwdUse = $db->getOne($query) > 0 ? true : false;
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("SRT_U","SRT_S");
        //ソートが押下
        if (trim(VARS::request("sort")) != "") {
            foreach ($sort as $field) {
                if (
                    VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0
                ) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                } elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                } else {
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }

        if (VARS::get("sort") == "SRT_U") {
            $this->sort["SRT_S"] = "";
        } elseif (VARS::get("sort") == "SRT_S") {
            $this->sort["SRT_U"] = "";
        }
    }

    //入力項目チェック
    public function validateInput()
    {

        //１レコード取得
        $row = knjz300Query::getRow($this->userscd, $this->year, $this);

        //ID重複チェック
        if (knjz300Query::chkID($this->userscd, $this->field["USERID"], $this)) {
            $this->setWarning("MSG302", "そのユーザーIDは既に使用されています。");
            return false;
        }
        if ($this->cmd == "update") {
            //ユーザコードチェック
            if ($row["STAFFCD"] != $this->field["USERSCD"]) {
                $this->setWarning("MSG303");
                return false;
            }
            //ユーザID入力チェック
            if ($this->field["USERID"] == "") {
                $this->setWarning("MSG901", "ユーザーIDを入力してください。");
                return false;
            }

            //パスワード入力チェック
            if ($this->field["PASSWD"] == "") {
                $this->setWarning("MSG901", "パスワードは１桁字以上入力してください。");
                return false;
            }

            //更新日付チェック
            if ($row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            //パスワードの履歴管理
            if ($this->validateInputHist()) {
                //passwd保持(変更しない時)
                $one = knjz300Query::getPass($this->field["USERSCD"], $this);
                $this->field["passwd"] = $one["PASSWD"];

                $this->delete();
                $this->insert();
                $this->setMessage("MSG201");
            }
        }
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        if ($this->delete()) {
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &insert()
    {
        $result = knjz300Query::getInsertQuery($this->field, $this);
        return $result;
    }

    //デリート時の内部メソッド
    public function &delete()
    {
        $result = knjz300Query::getDeleteQuery($this->field, $this);

        if ($this->cmd == "delete") {
            $result = knjz300Query::getDeleteUsergroupQuery($this->field, $this->year, $this);
        }
        return $result;
    }

    // 変数をクリアします
    public function &clean()
    {
    }

    //パスワードの履歴管理
    public function validateInputHist()
    {
        //パスワードが'**********'以外の時
        if (!stristr($this->field["PASSWD"], "*")) {
            //パスワードが不一致の時
            $one = knjz300Query::getPass($this->field["USERSCD"], $this);
            $passwd = md5($this->field["PASSWD"]);
            if ($passwd != $one["PASSWD"]) {
                //パスワードの履歴があればエラー
                $histCnt = knjz300Query::getPassHist($this->field["USERSCD"], $passwd);
                if ($histCnt > 0) {
                    $this->setWarning("MSG901", "過去に使用したパスワードは入力できません。");
                    return false;
                //パスワードの履歴がなければ、履歴にINSERT
                } else {
                    $result = knjz300Query::getInsertHist($this->field["USERSCD"], $passwd);
                }
            }
        }
        return true;
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $headerDuplicate = array();
            $headerDuplicate["SCHOOLCD"]    = "校種学校コード";
            $headerDuplicate["SCHOOL_KIND"] = "校種";
            $headerDuplicate["STAFFCD"]     = "職員コード";
            $tables = array();
            $tables = array("1" => "USER_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header[] = "※職員コード";
            $header[] = "職員氏名";
            $header[] = "職員氏名かな";
            $header[] = "※ユーザーＩＤ";
            $header[] = "無効フラグ";
            $header[] = "パスワード期限";
            if ($this->isChgPwdUse) {
                $header[] = "次回パスワード強制変更";
            }
            $header[] = $this->lastColumn;


            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz300Query::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= "1234567890,";
            $contents .= "広島　太郎,";
            $contents .= "ひろしま　たろう,";
            $contents .= "1234567890,";
            $contents .= "1,";
            $contents .= "1,";
            if ($this->isChgPwdUse) {
                $contents .= "1,";
            }
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="利用者マスタ一覧エラー";
            } else {
                $couse="利用者マスタ一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CVSファイル
    //クライアント ⇒ サーバ
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                knjz300Query::insertQueryCsv($db, $this, $this->data_arr);
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata["STAFFCD"]           = array(10 ,"職員コード");
            $checkdata["USERID"]            = array(32 ,"ユーザーＩＤ");
            $checkdata["INVALID_FLG"]       = array(1 ,"無効フラグ");
            $checkdata["PWDTERMCHK_FLG"]    = array(1 ,"パスワード期限");
            if ($this->isChgPwdUse) {
                $checkdata["CHG_PWD_FLG"]       = array(1 ,"次回パスワード強制変更");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query  = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('USER_MST');

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data["STAFFCD"]        = $tmp[0];
                    $data["USERID"]         = $tmp[3];
                    $data["INVALID_FLG"]    = $tmp[4];
                    $data["PWDTERMCHK_FLG"] = $tmp[5];
                    if ($this->isChgPwdUse) {
                        $data["CHG_PWD_FLG"]    = $tmp[6];
                    }

                    //エラーチェック
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
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;  //必須エラーフラグ
        $numericErrFlg      = false;  //数値エラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $valuesErrFlg       = false;  //その他エラーフラグ
        $masterErrFlg       = false;  //マスタエラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)
        $duplicateDbErrFlg  = false; //重複(DB)

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $valuesErrMsg       = "";     //その他エラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";    //重複(CSV)
        $duplicateDbErrMsg  = "";    //重複(DB)

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $duplicateDbSep  = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("STAFFCD", "USERID"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("STAFFCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //フラグの値チェック
            if (in_array($key, array("INVALID_FLG", "PWDTERMCHK_FLG", "CHG_PWD_FLG"))) {
                if ($data[$key] != "" && $data[$key] !== "0" && $data[$key] !== "1") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }
        }

        //存在チェック
        //職員
        $staff_cnt = $db->getOne(knjz300Query::checkStaff($data, $this));
        if ($staff_cnt == 0) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["STAFFCD"][1]);
        }
        //ユーザーＩＤ
        $Userid_cnt  = $db->getOne(knjz300Query::checkUserid($data));
        $Userid_cnt2 = $db->getOne(knjz300Query::checkUserid2($data));
        if ($Userid_cnt && !$Userid_cnt2) {
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz300Query::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
