<?php

require_once('for_php7.php');

require_once('knjd321cModel.inc');
require_once('knjd321cQuery.inc');

class knjd321cController extends Controller
{
    public $ModelClassName = "knjd321cModel";
    public $ProgramID      = "KNJD321C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv_data":        //データ出力
                    if (!$sessionInstance->getDataModel()) {
                        $this->callView("knjd321cForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjd321cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd321cCtl = new knjd321cController();
