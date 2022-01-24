<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja139aModel extends Model {
    var $field      = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja139aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択  //NO002
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }


    function getDownloadModel()
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $contents = ""; //CSVデータ出力用
        $data = false;
        
        //ヘッダ配列
        $header = array("学籍番号",
                        "生徒氏名",
                        "生徒ふりがな",
                        "住所郵便番号",
                        "住所",
                        "住所肩書き",
                        "電話番号",
                        "生年月日",
                        "性別",
                        "保護者氏名",
                        "保護者ふりがな",
                        "保護者住所郵便番号",
                        "保護者住所",
                        "保護者住所肩書き",
                        "保護者電話番号",
                        "入学・転編入日",
                        "入学種別",
                        "課程",
                        "学科",
                        "除籍日付",
                        "除籍区分",
                        "卒業日",
                        "出身中学校郵便番号",
                        "出身中学校名",
                        "出身中学卒業日",
                        "進路",
                        "受検番号",
                        "Ｅメールアドレス"
                        );

        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        $query = knja139aQuery::getCsvQuery($this->select_data["selectdata"]);
        $result = $db->query($query);
        
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $dataContents .= $row["SCHREGNO"] . $comma;
            $dataContents .= $row["NAME"] . $comma;
            $dataContents .= $row["NAME_KANA"] . $comma;
            $dataContents .= $row["ZIPCD"] . $comma;
            $dataContents .= $row["ADDR1"] . $comma;
            $dataContents .= $row["ADDR2"] . $comma;
            $dataContents .= $row["TELNO"] . $comma;
            $dataContents .= $row["BIRTHDAY"] . $comma;
            $dataContents .= $row["SEX"] . $comma;
            $dataContents .= $row["GUARD_NAME"] . $comma;
            $dataContents .= $row["GUARD_KANA"] . $comma;
            $dataContents .= $row["GUARD_ZIPCD"] . $comma;
            $dataContents .= $row["GUARD_ADDR1"] . $comma;
            $dataContents .= $row["GUARD_ADDR2"] . $comma;
            $dataContents .= $row["GUARD_TELNO"] . $comma;
            $dataContents .= $row["ENT_DATE"] . $comma;
            $dataContents .= $row["ENT_DIV_NAME"] . $comma;
            $dataContents .= $row["COURSENAME"] . $comma;
            $dataContents .= $row["MAJORNAME"] . $comma;
            $dataContents .= ($row["GRD_DIV"] != "1" ? $row["GRD_DATE"] : "") . $comma;
            $dataContents .= $row["GRD_DIV_NAME"] . $comma;
            $dataContents .= ($row["GRD_DIV"] == "1" ? $row["GRD_DATE"] : "") . $comma;
            $dataContents .= $row["J_ZIPCD"] . $comma;
            $dataContents .= $row["J_NAME"] . $comma;
            $dataContents .= $row["FINISH_DATE"] . $comma;
            
            $shinro = "";
            if ("0" == $row["SENKOU_KIND"]) {
                if ('' == $row["STAT_CD"] || '' != $row["E017NAME1"]) {
                } else {
                    $shinro .= $row["SCHOOL_NAME"];
                    $shinro .= ("000" == $row["FACULTYCD"] ? "" : $row["FACULTYNAME"]) . ("000" == $row["DEPARTMENTCD"] ? "" : $row["DEPARTMENTNAME"]);
                    $shinro .= $row["CAMPUSFACULTYADDR1"] != '' ? $row["CAMPUSFACULTYADDR1"] : $row["CAMPUSADDR1"];
                }
            } else if ("1" == $row["SENKOU_KIND"]) {
                if ('' == $row["STAT_CD"] || '' != $row["E018NAME1"]) {
                } else {
                    $shinro .= $row["COMPANY_NAME"];
                    $shinro .= $row["COMPANYADDR1"];
                    $shinro .= $row["COMPANYADDR2"];
                }
            }

            $dataContents .= $shinro . $comma;

            $dataContents .= $row["BASE_REMARK1"] . $comma;
            $dataContents .= $row["EMAIL"] . $comma;
            $dataContents .= $lf;

            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", "生徒情報");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
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
