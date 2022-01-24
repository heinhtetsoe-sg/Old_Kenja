<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd292wModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $hidSelected;

    /**
     * メンバ変数の初期化
     */
    public function knjd292wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "YEAR_DIV"         =>  VARS::post("YEAR_DIV"),         //出力年度区分
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->hidSelected = VARS::post("HID_SELECTED");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }


    public function getDownloadModel()
    {
        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ";
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJD";
        }
        $queryParams = array('cmd'      => 'csv',
                      'PRGID'           => "KNJD292W",
                      'DBNAME'          => DB_DATABASE,
                      'DOCUMENTROOT'    => DOCUMENTROOT,
                      'YEAR'            => CTRL_YEAR,
                      'CTRL_SEMESTER'   => CTRL_SEMESTER,
                      'LOGIN_DATE'      => CTRL_DATE,
                      'CLASS_SELECTED'  => $this->hidSelected
        );
        foreach ($this->field as $name => $value) {
            $queryParams[$name] = $value;
        }
        foreach (array(
                      "useCurriculumcd"
                    , "useVirus"
                    , "useKekkaJisu"
                    , "useKekka"
                    , "useLatedetail"
                    , "useKoudome"
                    , "useTestCountflg"
                    , "use_SchregNo_hyoji"
        ) as $name) {
            $queryParams[$name] = $this->Properties[$name];
        }

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($queryParams)
            )
        );
        $context  = stream_context_create($options);
        $queryContents = file_get_contents($url, false, $context);
        $title = "";
        $contents = "";
        if ($queryContents != false) {
            $map = json_decode($queryContents, true);
            $title = $map["TITLE"];
            foreach ($map["OUTPUT_LINES"] as $key => $csv) {
                $line = "";
                $comma = "";
                foreach ($csv as $col) {
                    $line .= $comma.$col;
                    $comma = ",";
                }
                $contents .= $line."\n";
            }
        }

        if ($contents) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
