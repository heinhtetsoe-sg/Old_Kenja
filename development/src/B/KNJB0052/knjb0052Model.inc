<?php

require_once('for_php7.php');

class knjb0052Model extends Model
{
    public $semester;
    public $field      = array();
    public $cmd;
    public $selectData;
    public $control;        //コントロールマスタ

    public function knjb0052Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        $this->cmd          = VARS::request("cmd");
        $this->selectData   = VARS::post("selectData");

        $this->field = array("YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),   //年度 学期
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),     //対象日付
                             "OUT_DIV"          =>  VARS::post("OUT_DIV"),         //対象
                             "ORDER_DIV"        =>  VARS::post("ORDER_DIV"),       //出力順
                             "KIJIKU_CHAIRCD"   =>  VARS::post("KIJIKU_CHAIRCD"),  //基軸講座コード
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }
        $queryParams = array(
            'cmd'             => 'csvContents',
            'YEAR'            => CTRL_YEAR,
            'CTRL_SEMESTER'   => CTRL_SEMESTER,
            'CTRL_DATE'       => str_replace("-", "/", CTRL_DATE),
            'DOCUMENTROOT'    => DOCUMENTROOT,
            'PRGID'           => "KNJB0052",
            'SCHOOLCD'        => sprintf("%012d", SCHOOLCD),
            'SCHOOL_KIND'     => SCHOOLKIND,
            'category_selected' => $this->selectData
        );
        foreach ($this->field as $name => $value) {
            $queryParams[$name] = $value;
        }
        foreach (array("useCurriculumcd",
                      "useSchool_KindField",
                      "use_prg_schoolkind",
                      "selectSchoolKind") as $name) {
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
        $arr = array();
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

        //CSV出力
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
