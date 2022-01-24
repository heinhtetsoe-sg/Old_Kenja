<?php

require_once('for_php7.php');

require_once('knjl420Model.inc');
require_once('knjl420Query.inc');

class knjl420Controller extends Controller {
    var $ModelClassName = "knjl420Model";
    var $ProgramID      = "KNJL420";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeYear":
                case "changeKind":
                case "kousinZumi":
                case "ret421":
                case "ret420_1":
                case "ret420_2":
                case "reset":
                case "fuban":
                case "fubanClear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjl420Form1");
                   break 2;
                case "add":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "update":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "delete":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "sendDel":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getSendDeleteModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_PRISCHOOL/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&prischoolcd=1";
                    $args["left_src"] .= "&prischool_name=1";
                    $args["left_src"] .= "&prischool_kana=1";
                    $args["left_src"] .= "&prischool_class_name=1";
                    $args["left_src"] .= "&prischool_class_kana=1";
                    $args["left_src"] .= "&rosen_name=1";
                    $args["left_src"] .= "&nearest_station_name=1";
                    $args["left_src"] .= "&districtcd=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["left_src"] .= "&PATH=" .urlencode("/L/KNJL420/knjl420index.php?cmd=main");
                    $args["right_src"] = "knjl420index.php?cmd=main";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
    
        //権限チェック
    function checkAuth2($auth, $view=""){
        $model =& Model::getModel($this);

        if ($model->auth < $auth){
            $model->setWarning("MSG300");
            if ($view == ""){
                $view = $model->_view;
            }
            //直前に表示されたフォームを表示
            $this->callView($view);
            exit;
        }
    }
}
$knjl420Ctl = new knjl420Controller;
?>
