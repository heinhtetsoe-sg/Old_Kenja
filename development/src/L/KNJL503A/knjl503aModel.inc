<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl503aModel extends Model {

    var $field = array();
    var $cmd;
    var $examyear;
    var $tmp;
    var $file;
    var $fstSchCd; //※画面左のリンクをクリックしたときの情報。更新時のチェックで利用。
    var $finschoolType;

    //メンバ変数の初期化

    function knjl503aModel() {
    }

    function init() {
        $this->examyear = CTRL_YEAR + 1;
        $this->file = VARS::file("FILE");
        $this->cmd     = VARS::request("cmd");

        if (VARS::post("FINSCHOOL_TYPE")) {
            $this->finschoolType = VARS::post("FINSCHOOL_TYPE");
        }

        $this->field = array("ENTEXAM_SCHOOLCD"   =>  VARS::post("ENTEXAM_SCHOOLCD"),    //職員コード
                             "FINSCHOOLCD"        =>  VARS::post("FINSCHOOLCD"),         //職員カナ
                             "HEADER"             =>  VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"             =>  VARS::post("OUTPUT")               //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        if (VARS::get("ENTEXAM_SCHOOLCD")) {
            $this->field["ENTEXAM_SCHOOLCD"] = VARS::get("ENTEXAM_SCHOOLCD");
            $this->fstSchCd = VARS::get("ENTEXAM_SCHOOLCD");
        }
        if ($this->cmd == "reset") {
            unset($this->fstSchCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //add/update用。

        //必須入力チェック
        //  愛知県学校コード
        if ($this->field["ENTEXAM_SCHOOLCD"] == "" || $this->field["FINSCHOOLCD"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        //出身学校コード登録件数取得
        $row1 = "";
        $fstrow = knjl503aQuery::getRow($this, $this->fstSchCd);
        $db = Query::dbCheckOut();
        $row1 = $db->getOne(knjl503aQuery::checkExist($this->field, $this, "CHK_FINSCH"));
        $row2 = $db->getOne(knjl503aQuery::checkExist($this->field, $this));
        Query::dbCheckIn($db);
        if ($this->cmd == "add" && $row1 >= "1") {
            $this->setWarning("MSG302", "入力した出身学校コードが既に登録されています。");
            return false;
        }
        if ($this->cmd == "update") {
            if (!is_array($fstrow)) {
                $this->setWarning("MSG308");
                return false;
            }
            //出身学校コードを中心にチェックする。
            //選択している場合としていない場合で分けているが、これは出身学校コードが登録されいている場合となる。
            //選択していない場合、たまたま出身学校コードが登録されいている場合とされていない場合に分かれる。
            //※出身学校コードが登録されている場合でも"登録データと一致"なら見逃す。
            if ($row1 > 0) {
                //選択している場合、$fstrowでチェック。
                if (is_array($fstrow)) {
                    //修正前データと一致しないならエラー(一致なら見逃す)。
                    //※完全一致についてのチェックは後のチェックでもやっているので、ここでは"選択と入力が一致"のみチェックしている。
                    //ここでNGなら、"選んだデータから変更して違うデータと被った"となるので、そのデータを選択して更新してもらうのが正。
                    if (!($fstrow["FINSCHOOLCD"] == $this->field["FINSCHOOLCD"] && $fstrow["ENTEXAM_SCHOOLCD"] == $this->field["ENTEXAM_SCHOOLCD"])) {
                        $this->setWarning("MSG308");
                        return false;
                    }
                }
            } else {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //１レコード取得
        $row = "";
        $row = knjl503aQuery::getRow($this);

        if ($this->cmd == "add") {
            //追加時に既に登録されていたらエラー
            if (is_array($row)) {
                if ($row1 > 0) {
                    $this->setWarning("MSG302", "出身学校コードが重複しています。");
                } else {
                    $this->setWarning("MSG302", "愛知県学校コードが重複しています。");
                }
                return false;
            }
        }

        //桁数チェック
        //  愛知県学校
        if (strlen($this->field["ENTEXAM_SCHOOLCD"]) > 4 ) {
            $this->setWarning("MSG915", "(愛知県学校コード) 4バイトまでです。");
            return false;
        }

        //  出身学校コード
        if (strlen($this->field["FINSCHOOLCD"]) > 12 ) {
            $this->setWarning("MSG915", "(出身学校コード) 12バイトまでです。");
            return false;
        }

        return true;

    }

    function &getInsertModel() {
        if ($this->validateInput()) {
            $this->insert();
            $this->setMessage("MSG201");
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    //削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->delete()) {
            $this->setMessage("MSG201");
        }
     }

    //アップデート時の内部メソッド
    function &insert() {
        $result = knjl503aQuery::getInsertQuery($this);
        return $result;
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjl503aQuery::getUpdateQuery($this);
        return $result;
    }

    //デリート時の内部メソッド
    function &delete() {
        $result = knjl503aQuery::getDeleteQuery($this);
        return $result;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度と前年度のデータ存在チェック
        $check = knjl503aQuery::CheckData($this);

        if (in_array('X',$check)) {
            $this->setWarning('MSG203', "今年度のデータが存在します。");
            return false;
        } elseif (!in_array('Y',$check)) {
            $this->setWarning('MSG203', "前年データが存在しません。");
            return false;
        } else{
            knjl503aQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    // 変数をクリアします
    function &clean() {
        unset($this->field);
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    function getDownloadCsvModel() {
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $contents = implode(",", $header) . "\r\n";
            $query = knjl503aQuery::getErrorCsvData();
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header[] = "※愛知県学校コード";
            $header[] = "出身学校名";
            $header[] = "出身学校コード";


            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjl503aQuery::getList($this, "csv");
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= "0001,";
            $contents .= "テスト学校,";
            $contents .= "000000000000\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse=$this->examyear."年度入試用出身学校マスタエラー";
            } else {
                $couse=$this->examyear."年度入試用出身学校マスタ";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }

    }

    //CVSファイル
    //クライアント ⇒ サーバ
    function getUploadCsvModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $cnt = knjl503aQuery::insertQueryCsv($db, $this, $this->data_arr);
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL503A'");
            Query::dbCheckIn($db);
            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //送られたファイルの文字列を取得
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata["ENTEXAM_SCHOOLCD"]     = array(4 ,"愛知県学校コード");
            $checkdata["ENTEXAM_SCHOOLNAME"]   = array(25*2 ,"出身学校名");  //登録はしないが、入力補助として出力している項目。
            $checkdata["FINSCHOOLCD"]          = array(12 ,"出身学校コード");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query  = knjl503aQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
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
                    $data["ENTEXAM_SCHOOLCD"]   = $tmp[0];
                    $data["ENTEXAM_SCHOOLNAME"] = $tmp[1];
                    $data["FINSCHOOLCD"]        = $tmp[2];

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg      = true;

        $hissuErrFlg     = false;  //必須エラーフラグ
        $numericErrFlg   = false;  //数値エラーフラグ
        $lengthErrFlg    = false;  //レングスエラーフラグ
        $otherErrFlg     = false;  //その他エラーフラグ
        $existenceErrFlg = false;  //存在エラーフラグ

        $hissuErrMsg     = "";     //必須エラーメッセージ
        $numericErrMsg   = "";     //数値エラーメッセージ
        $lengthErrMsg    = "";     //レングスエラーメッセージ
        $otherErrMsg     = "";     //その他エラーメッセージ
        $existenceErrMsg = "";     //存在エラーメッセージ

        $hissuSep       = "";
        $numericSep     = "";
        $lengthSep      = "";
        $otherSep       = "";
        $existenceSep   = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("ENTEXAM_SCHOOLCD", "FINSCHOOLCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            // //数値チェック
            // if (in_array($key,array("STAFFCD"))) {
            //     if ($data[$key] != "" && !is_numeric($data[$key])) {
            //         $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            //     }
            // }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            // //フラグの値チェック
            // if (in_array($key,array("INVALID_FLG", "PWDTERMCHK_FLG", "CHG_PWD_FLG"))) {
            //     if ($data[$key] != "" && $data[$key] !== "0" && $data[$key] !== "1") {
            //         $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※フラグの値:", $otherSep, $val[1]);
            //     }
            // }
        }

        //存在チェック(出身学校コードがマスタに未登録ならエラー)
        $dat_cnt = $db->getOne(knjl503aQuery::checkFinSchCdExist($data, $this));
        if ($dat_cnt == 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $checkdata["FINSCHOOLCD"][1]);
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl503aQuery::insertQueryErr($db, $line, $check_error);
        }

        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
?>
