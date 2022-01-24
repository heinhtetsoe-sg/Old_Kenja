<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615qModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd615qModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "MAJOR"            =>  VARS::post("MAJOR"),            //学科
                             "KETTEN"           =>  VARS::post("KETTEN"),           //欠点
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "selectlist"       =>  VARS::post("selectlist"),       //選択リスト
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

//データ出力
    function getCsvModel()
    {
        $param = array();
        foreach ($_POST as $name => $val) {
            $param[$name] = $val;
        }
        $param["cmd"] = "csv";

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($param)
            )
        );
        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJD";
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJD";
        }
        $result = file_get_contents($url, false, stream_context_create($options));
        if ($result == FALSE) {
            $this->warning = "該当データは、ありません。";
            return false;
        } else {
            $json = json_decode($result, true);
            $filename = $json["FILENAME"];
            $contents = "";
            foreach ($json["OUTPUT_LINES"] as $arr) {
                $contents .= implode(',', $arr)."\n";
            }
            common::downloadContents($contents, $filename);
            return true;
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
