<?php

require_once('knjh442cModel.inc');
require_once('knjh442cQuery.inc');

class knjh442cController extends Controller
{
    public $ModelClassName = "knjh442cModel";
    public $ProgramID      = "KNJH442C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh442c":
                    $sessionInstance->knjh442cModel();
                    $this->callView("knjh442cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjh442cForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh442cCtl = new knjh442cController();
