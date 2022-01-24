<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143qModel extends Model {
    var $field      = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja143qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "MIBUNSYOKKEN"     =>  VARS::post("MIBUNSYOKKEN"),     //身分証、食券選択
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択  //NO002
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }


    function getDownloadModel() {
        if ($this->field["MIBUNSYOKKEN"] == "1") {
            //CSVデータ抽出処理()
            //DB接続
            $db = Query::dbCheckOut();
            $lf = "\n";
            $comma = ",";
            $contents = ""; //CSVデータ出力用
            $data = false;
            // 生徒住所履歴 //履歴を降順に読み込み、最大３件まで出力
            $query = knja143qQuery::getCsvSql1($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $contents .= $row["MIBUNSHO_BANGOU"];
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0);
                $contents .= $comma.$row["NAME"];
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["ENT_DATE"]),0);
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["LIMIT"]),0);
                $contents .= $comma.$row["SCHREGNO"];
                $contents .= $comma."0";
                $contents .= $comma.$row["MIBUNSHO_SEX_CD"];
                $contents .= $comma.$row["ENT_MONTH_CD"];
                $contents .= $comma.$row["BIRTHDAYCD"];
                $contents .= $comma."";
                $contents .= $comma."1";
                $contents .= $lf;
                $data = true;
            }

            $result->free();

            //DB切断
            Query::dbCheckIn($db);

            if ($data) {
                $header = "身分証番号";
                $header .= $comma."生年月日(和暦)";
                $header .= $comma."氏名";
                $header .= $comma."入学年月日";
                $header .= $comma."有効期限";
                $header .= $comma."個人コード";
                $header .= $comma."紛失回数(初期値)";
                $header .= $comma."性別";
                $header .= $comma."入学年月コード";
                $header .= $comma."生年月日コード";
                $header .= $comma."写真パス";
                $header .= $comma."発行枚数";
                $header .= $lf;

                //$contents = $header.$contents; // ヘッダ

                $couse="生徒証ＣＳＶ出力磁気あり";
                $filename = sprintf("%s.csv", $couse);
                common::downloadContents($contents, $filename, "SJIS-win");
                return true;
            } else {
                $this->setWarning("MSG303");
                return false;
            }
        } else if ($this->field["MIBUNSYOKKEN"] == "2") {
            //CSVデータ抽出処理()
            //DB接続
            $db = Query::dbCheckOut();
            $lf = "\n";
            $comma = ",";
            $contents = ""; //CSVデータ出力用
            $data = false;
            // 生徒住所履歴
            $query = knja143qQuery::getCsvSql1($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $contents .= $row["MIBUNSHO_BANGOU"];
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["BIRTHDAY"]),0);
                $contents .= $comma.$row["NAME"];
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["ENT_DATE"]),0);
                $contents .= $comma.common::DateConv1(str_replace("-","/",$row["LIMIT"]),0);
                $contents .= $comma.$row["SCHREGNO"];
                $contents .= $comma."";
                $contents .= $comma."1";
                $contents .= $lf;
                $data = true;
            }

            $result->free();

            //DB切断
            Query::dbCheckIn($db);

            if ($data) {
                $header = "身分証番号";
                $header .= $comma."生年月日(和暦)";
                $header .= $comma."氏名";
                $header .= $comma."入学年月日";
                $header .= $comma."有効期限";
                $header .= $comma."個人コード";
                $header .= $comma."写真パス";
                $header .= $comma."発行枚数";
                $header .= $lf;

                //$contents = $header.$contents; // ヘッダ

                $couse="生徒証ＣＳＶ出力磁気なし";
                $filename = sprintf("%s.csv", $couse);
                common::downloadContents($contents, $filename, "SJIS-win");
                return true;
            } else {
                $this->setWarning("MSG303");
                return false;
            }
        } else if ($this->field["MIBUNSYOKKEN"] == "3") {
            //CSVデータ抽出処理
            //DB接続
            $db = Query::dbCheckOut();
            $lf = "\n";
            $comma = ",";
            $contents = ""; //CSVデータ出力用
            $data = false;
            // 生徒住所履歴 //履歴を降順に読み込み、最大３件まで出力
            $query = knja143qQuery::getCsvSql3($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",",".",$row);

                $contents .= "";//通番
                $contents .= $comma.$row["BASE_REMARK1"];//受験番号
                $contents .= $comma.$row["SCHREGNO"];//学籍番号
                $contents .= $comma.ltrim($row["GRADE_CD"], "0");//学年
                $contents .= $comma.$row["HR_CLASS_NAME1"];//組
                $contents .= $comma.ltrim($row["GRADE_CD"], "0").$row["HR_CLASS_NAME1"];//学年組
                $contents .= $comma.ltrim($row["ATTENDNO"], "0");//番号
                $contents .= $comma.$row["COURSECODENAME"];//コース
                $contents .= $comma.$row[""];//外字対応
                $contents .= $comma.$row["NAME"];//氏名
                $contents .= $comma.$row["NAME_KANA"];//ﾌﾘｶﾞﾅ
                $contents .= $comma.$row["SEX"];//性別
                $contents .= $comma.substr(str_replace("-","",$row["BIRTHDAY"]), 2, 6);//生年月日
                $contents .= $comma.$row["PREF"];//県名
                $contents .= $comma.$row["FINSCHOOLCD"];//中学校コード
                $contents .= $comma.$row["FINSCHOOL_NAME"];//中学校名１
                $contents .= $comma.str_replace("-","",$row["ZIPCD"]);//生徒郵便番号
                $contents .= $comma.$row["ADDR1"];//生徒住所１
                $contents .= $comma.$row["ADDR2"];//生徒住所２
                $contents .= $comma.$row["TELNO"];//生徒電話
                $contents .= $comma.str_replace("-","",$row["GUARD_ZIPCD"]);//保護者郵便番号
                $contents .= $comma.$row["GUARD_ADDR1"];//保護者住所１
                $contents .= $comma.$row["GUARD_ADDR2"];//保護者住所２
                $contents .= $comma.$row["GUARD_TELNO"];//保護者電話番号
                $contents .= $comma.$row["GUARD_NAME"];//保護者名
                $contents .= $comma.$row["GUARD_KANA"];//保護者ﾌﾘｶﾞﾅ
                $contents .= $comma.$row["RELATIONSHIP"];//続柄
                $contents .= $comma.$row["RYO"];//寮
                $contents .= $comma.$row["RYONO"];//寮番号
                $contents .= $comma.$row["TOKUTAI"];//特待
                $contents .= $comma.$row["SUKARA"];//スカラー種別
                $contents .= $comma.ltrim($row["GRADE_CD"], "0").$row["HR_CLASS_NAME1"].ltrim($row["ATTENDNO"], "0");//当年クラス
                $contents .= $comma."";//前年クラス
                $contents .= $comma."";//前々年クラス
                $contents .= $comma."";//中学卒業年
                $contents .= $comma.str_replace("-","/",$row["NYUGAKU"]);//入学年月日
                $contents .= $comma.str_replace("-","/",$row["SOTUGYO"]);//卒業年月日
                $contents .= $comma.str_replace("-","/",$row["TENNYU"]);//転入年月日
                $contents .= $comma.str_replace("-","/",$row["TENGAKU"]);//転学年月日
                $contents .= $comma.str_replace("-","/",$row["TAIGAKU"]);//退学年月日
                $contents .= $comma.$row["ENT_SCHOOL"];//転入元
                $contents .= $comma.$row["GRD_SCHOOL"];//転学先
                $contents .= $comma.$row["BIKOU"];//備考
                $contents .= $comma.$row["SUNSYO"];//駿小出身
                $contents .= $comma."";//所属
                $contents .= $comma.$row["GRADE"];//SubID
                $contents .= $comma."";//P前々年
                $contents .= $comma."";//P前年
                $contents .= $comma."";//P当年
                $contents .= $comma."";//防災
                $contents .= $comma.$row["ZAISEKI"];//在籍
                $contents .= $lf;
                $data = true;
            }

            $result->free();

            //DB切断
            Query::dbCheckIn($db);

            if ($data) {
                $header  = "通番";
                $header .= $comma."受験番号";
                $header .= $comma."学籍番号";
                $header .= $comma."学年";
                $header .= $comma."組";
                $header .= $comma."学年組";
                $header .= $comma."番号";
                $header .= $comma."コース";
                $header .= $comma."外字対応";
                $header .= $comma."氏名";
                $header .= $comma."ﾌﾘｶﾞﾅ";
                $header .= $comma."性別";
                $header .= $comma."生年月日";
                $header .= $comma."県名";
                $header .= $comma."中学校コード";
                $header .= $comma."中学校名１";
                $header .= $comma."生徒郵便番号";
                $header .= $comma."生徒住所１";
                $header .= $comma."生徒住所２";
                $header .= $comma."生徒電話";
                $header .= $comma."保護者郵便番号";
                $header .= $comma."保護者住所１";
                $header .= $comma."保護者住所２";
                $header .= $comma."保護者電話番号";
                $header .= $comma."保護者名";
                $header .= $comma."保護者ﾌﾘｶﾞﾅ";
                $header .= $comma."続柄";
                $header .= $comma."寮";
                $header .= $comma."寮番号";
                $header .= $comma."特待";
                $header .= $comma."スカラー種別";
                $header .= $comma."当年クラス";
                $header .= $comma."前年クラス";
                $header .= $comma."前々年クラス";
                $header .= $comma."中学卒業年";
                $header .= $comma."入学年月日";
                $header .= $comma."卒業年月日";
                $header .= $comma."転入年月日";
                $header .= $comma."転学年月日";
                $header .= $comma."退学年月日";
                $header .= $comma."転入元";
                $header .= $comma."転学先";
                $header .= $comma."備考";
                $header .= $comma."駿小出身";
                $header .= $comma."所属";
                $header .= $comma."SubID";
                $header .= $comma."P前々年";
                $header .= $comma."P前年";
                $header .= $comma."P当年";
                $header .= $comma."防災";
                $header .= $comma."在籍";
                $header .= $lf;

                //$contents = $header.$contents; // ヘッダ

                $couse="T_生徒";
                $filename = sprintf("%s.csv", $couse);
                common::downloadContents($contents, $filename, "SJIS-win");
                return true;
            } else {
                $this->setWarning("MSG303");
                return false;
            }
        }
    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
