<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp820Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjp820Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("SELYEAR"     =>  VARS::post("SELYEAR") ? VARS::post("SELYEAR") : CTRL_YEAR,     //年度
                             "SELSCHKIND"  =>  VARS::post("SELSCHKIND"),  //校種
                             "SELGRADE"    =>  VARS::post("SELGRADE"),    //学年
                             "SELDATE"     =>  VARS::post("SELDATE"),     //異動対象日
                             "HEADER"      =>  VARS::post("HEADER")       //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }

    function &getMainModel() {
    }

    //入力チェック
    function validateInput() {

        return true;
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["SIGEL_NO"]                = '個人番号';
        $header["SEI"]                     = '姓';
        $header["MEI"]                     = '名';
        $header["KANA_SEI"]                = '姓（ｶﾅ）';
        $header["KANA_MEI"]                = '名（ｶﾅ）';
        $header["ENG_SEI"]                 = '姓（英語）';
        $header["ENG_MEI"]                 = '名（英語）';
        $header["SEX"]                     = '性別';
        $header["BIRTHDAY"]                = '生年月日';
        $header["NENREI"]                  = '5月1日年齢';
        $header["HANDICAP_TYPE"]           = '身障者区分';
        $header["COUNTRY_CD"]              = '国籍CD';
        $header["COME_FROM_PREFCD"]        = '出身都道府県CD';
        $header["REGION"]                  = '地域';
        $header["CHG_CD"]                  = '変更CD';
        $header["ZIP_NO"]                  = '郵便番号';
        $header["PREF_CD"]                 = '都道府県CD';
        $header["ADDRESS1"]                = '住所１';
        $header["ADDRESS2"]                = '住所２';
        $header["ADDRESS3"]                = '住所３';
        $header["TELNO"]                   = '電話番号';
        $header["MOBILE_TELNO"]            = '携帯番号';
        $header["FAXNO1"]                  = 'FAX番号1';
        $header["FAXNO2"]                  = 'FAX番号2';
        $header["MAILADDRESS1"]            = 'メールアドレス1';
        $header["MAILADDRESS2"]            = 'メールアドレス2';
        $header["MOBILE_MAIL_ADDR"]        = '携帯メールアドレス';
        $header["FINSCHOOLCD"]             = '出身校CD';
        $header["FINISH_DATE"]             = '出身校卒業年月日';
        $header["FINSCHOOL_MAJORCD"]       = '出身学科CD';
        $header["FOMSCHOOL_COURSECODE"]    = '出身課程CD';
        $header["PHOTO"]                   = '写真';
        $header["VER_NO"]                  = 'バージョン番号';

        $header["WHOLENO"]                 = '生涯番号';
        $header["SGL_SCHOOLKIND"]          = '設置';
        $header["SGL_MAJORCD"]             = '学部';
        $header["SGL_COURSECODE"]          = '学科';
        $header["MAJOR_SUBJECT"]           = '専攻';
        $header["COURSECODE"]              = 'コース';
        $header["SCHREGNO"]                = '学籍番号';
        $header["ENROLL_STAT_TYPE"]        = '在籍状態区分';
        $header["ENROLL_TYPE"]             = '在籍区分';
        $header["STUDENT_TYPE"]            = '学生種別';
        $header["STUDENT_KUBUN"]           = '学生区分';
        $header["GRADE"]                   = '学年';
        $header["GRADER"]                  = '回生';
        $header["RESIDENCE_TYPE"]          = '在留資格区分';
        $header["OVERSEAS_TYPE"]           = '留学生区分';
        $header["ENT_TYPE"]                = '入学区分';
        $header["ENT_YEAR"]                = '入学年度';
        $header["HALFMONTH_GRAD_FLG"]      = '9月卒業判定区分';

        $header["GRD_YEAR"]                = '卒業年度';
        $header["TRANSFER_DATE"]           = '転入年月日';
        $header["DROPOUT_DATE"]            = '退学除籍年月日';
        $header["EXAMNO"]                  = '受験番号';
        $header["CONTACT_PROF"]            = '連絡先者職業';
        $header["OFFICE_NAME"]             = '勤務先名（漢字）';
        $header["OFFICE_KANA"]             = '勤務先名（カナ）';
        $header["TUITION_SEND_TYPE"]       = '学費発送区分';
        $header["DEPOSIT_TYPE"]            = '振込区分';
        $header["GRACE_TYPE"]              = '猶予区分';
        $header["LATE_TERM_DEPOSIT_TYPE"]  = '振込区分後期';
        $header["LATE_TERM_GRACE_TYPE"]    = '猶予区分後期';

        $header["ENROLL_STATE"]            = '学籍状況';
        $header["OVERSEAS_REDUCTION_TYPE"] = '留学生減免区分';
        $header["SPORT_SPEC_TYPE"]         = 'スポーツ特待生区分';
        $header["PAYMENT_TIMES"]           = '納入回数';
        $header["EXTRA_ENROLL_TYPE"]       = '在学期間延長区分';
        $header["REMARK"]                  = '備考';
        $header["BANK_CD"]                 = '銀行CD';
        $header["BANK_KANA"]               = '銀行名(カナ)';
        $header["BANK_BRANCH_CD"]          = '銀行支店CD';
        $header["BANK_BRANCH_NAME"]        = '銀行支店名(カナ)';
        $header["BANK_DEPOSIT_TYPE"]       = '預金種別区分';
        $header["BANK_ACCOUNT_NO"]         = '口座番号';
        $header["BANK_ACCOUNT_NAME"]       = '口座名（カナ）';
        $header["BANK_ACCOUNT_REMARK"]     = '口座備考';
        $header["STUDENT_CTRL_NO"]         = '学生管理番号';
        $header["ENT_SEMESTER"]            = '入学期';

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjp820Query::selectQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $array = $header;
                $row["GNAME"] = str_replace(" ", "　", $row["GNAME"]);
                $row["GKANA"] = str_replace(" ", "　", $row["GKANA"]);

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    if (array_key_exists($key, $row)) {
                        if ($key === "BIRTHDAY" || $key === "ZIP_NO" || $key === "DROPOUT_DATE") {
                            $row[$key] = str_replace("-", "", $row[$key]);
                        } else if ($key === "ADDRESS1") {
                            $tmp1 = str_replace($row["PREF_NAME"], "", $row[$key]);
                            $addArr = array();
                            if ($row["CITY"] != '') {
                                $addArr = explode("{$row["CITY"]}", $tmp1);
                                $row[$key] = $addArr[0].$row["CITY"];
                            }
                        } else if ($key === "ADDRESS2") {
                            if ($row["CITY"] != '') {
                                $row[$key] = $addArr[1];
                            }
                        }
                        $contents .= $comma.$row[$key];
                    } else {
                        if ($key === "SEI") {
                            $cutwk = explode("　", $row["GNAME"]);
                            $setSei = trim($cutwk[0]);
                            $contents .= $comma.$setSei;
                        } else if ($key === "MEI") {
                            $cutwk = explode("　", $row["GNAME"], 2);
                            $cutwk[1] = str_replace("　", " ", $cutwk[1]);
                            $setMei = trim($cutwk[1]);
                            $contents .= $comma.$setMei;
                        } else if ($key === "KANA_SEI") {
                            $cutwk = explode("　", mb_convert_kana($row["GKANA"], "hk"));
                            $setKanaSei = trim($cutwk[0]);
                            $contents .= $comma.$setKanaSei;
                        } else if ($key === "KANA_MEI") {
                            $cutwk = explode("　", mb_convert_kana($row["GKANA"], "hk"), 2);
                            $cutwk[1] = str_replace("　", " ", $cutwk[1]);
                            $setKanaMei = trim($cutwk[1]);
                            $contents .= $comma.$setKanaMei;
                        } else {
                            $contents .= $comma;
                        }
                    }
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;

                //COLLECT_SGL_SCHREGNO_MAPPING_DAT作成
                knjp820Query::InsertQuery($db, $this, $row);
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "学籍情報一括登録・保証人情報 出力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
}
?>
