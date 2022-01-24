<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc163Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $selectdata;

    public function knjc163Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SEMESTER"    =>  VARS::post("SEMESTER"),    //学期
                             "SDATE"       =>  VARS::post("SDATE"),       //対象日(開始)
                             "EDATE"       =>  VARS::post("EDATE"),       //対象日(終了)
                             "DATE"        =>  VARS::post("DATE"),        //学籍処理日
                             "OUTPUT"      =>  VARS::post("OUTPUT"),        //欠席日数・指定した割合
                             "KESSEKI_NISSU" =>  VARS::post("KESSEKI_NISSU"),        //欠席日数・指定した割合
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                             "TYUI_TYOUKA" =>  VARS::post("TYUI_TYOUKA"), //注意・超過ラジオボタン
                             "SCHKIND"     =>  VARS::post("SCHKIND")      //校種
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

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
            'CTRL_YEAR'       => CTRL_YEAR,
            'CTRL_SEMESTER'   => CTRL_SEMESTER,
            'CTRL_DATE'       => str_replace("-", "/", CTRL_DATE),
            'DBNAME'          => DB_DATABASE,
            'PRGID'           => "KNJC163",
            'DOCUMENTROOT'    => DOCUMENTROOT,
            'SEME_SDATE'      => $this->control["学期開始日付"][$this->field["SEMESTER"]],
            'SEME_EDATE'      => $this->control["学期終了日付"][$this->field["SEMESTER"]],
            'CATEGORY_SELECTED'  => $this->selectdata
        );
        foreach ($this->field as $name => $value) {
            $queryParams[$name] = $value;
        }
        foreach (array("useCurriculumcd",
                      "useVirus",
                      "useKekkaJisu",
                      "useKekka",
                      "useLatedetail",
                      "useKoudome",
                      "use_SchregNo_hyoji",
                      "useTestCountflg",
                      "knjc163useAttendSemesDat",
                      "hibiNyuuryokuNasi") as $name) {
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
